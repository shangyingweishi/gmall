package com.gm.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gm.gmall.bean.OmsOrder;
import com.gm.gmall.bean.OmsOrderItem;
import com.gm.gmall.mq.ActiveMQUtil;
import com.gm.gmall.order.mapper.OmsOrderItemMapper;
import com.gm.gmall.order.mapper.OmsOrderMapper;
import com.gm.gmall.service.CartService;
import com.gm.gmall.service.OrderService;
import com.gm.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Reference
    CartService cartService;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public OmsOrder getOrderByTradeNum(String tradeNum) {

        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(tradeNum);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);

        return omsOrder1;
    }

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {

        Jedis jedis = null;

        try {

            jedis = redisUtil.getJedis();

            String tradeKey = "user:" + memberId + ":tradeCode";

            String tradeCodeFromCache = jedis.get(tradeKey);

            //使用lua脚本在发现key的同时将key删除，防止并发订单攻击
            //对比防重删令牌
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey),
                    Collections.singletonList(tradeCode));


            if(eval != null && eval != 0){
 //           if (StringUtils.isNotBlank(tradeCodeFromCache) && tradeCodeFromCache.equals(tradeCode)) {

                //jedis.del(tradeCode);//使用lua脚本在发现key的同时将key删除，防止并发订单攻击

                return "success";

            } else {

                return "fail";

            }
        } finally {

            jedis.close();
        }


    }

    @Override
    public String genTradeCode(String memberId) {

        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();

            String tradeKey = "user:" + memberId + ":tradeCode";

            String tradeCode = UUID.randomUUID().toString();

            jedis.setex(tradeKey, 60 * 15, tradeCode);

            return tradeCode;
        } finally {

            jedis.close();

        }


    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {

        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();

        //保存订单详情表
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);

            //删除购物车数据
            //cartService.delCart();
        }

    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {

        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());
        OmsOrder omsOrderUpdate = new OmsOrder();
        omsOrderUpdate.setStatus(2);

        //发送一个订单已支付的队列，提供给库存消费
        Connection connection = null;
        Session session = null;
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            Queue order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(order_pay_queue);
            TextMessage textMessage = new ActiveMQTextMessage();

            //查询订单对象，转换成json字符串，存入ORDER_PAY_QUERE的消息队列
            OmsOrder omsOrder1 = new OmsOrder();
            omsOrder1.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResponse = omsOrderMapper.selectOne(omsOrder1);
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderSn(omsOrder1.getOrderSn());
            List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItem);
            omsOrderResponse.setOmsOrderItems(omsOrderItems);
            textMessage.setText(JSON.toJSONString(omsOrderResponse));

            omsOrderMapper.updateByExampleSelective(omsOrderUpdate, example);
            producer.send(textMessage);
            session.commit();


        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (JMSException e) {
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

}
