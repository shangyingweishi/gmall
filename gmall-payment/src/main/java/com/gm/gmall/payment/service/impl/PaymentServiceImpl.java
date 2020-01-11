package com.gm.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.gm.gmall.bean.PaymentInfo;
import com.gm.gmall.mq.ActiveMQUtil;
import com.gm.gmall.payment.mapper.PaymentInfoMapper;
import com.gm.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        //幂等性检查，防重复更新
        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo selectOne = paymentInfoMapper.selectOne(paymentInfoParam);
        if (StringUtils.isNotBlank(selectOne.getPaymentStatus()) && selectOne.getPaymentStatus().equals("已支付")) {

            return;


        } else {
            Example example = new Example(PaymentInfo.class);
            example.createCriteria().andEqualTo("orderSn", paymentInfo.getOrderSn());

            Connection connection = null;
            Session session = null;
            try {
                connection = activeMQUtil.getConnectionFactory().createConnection();
                session = connection.createSession(true, Session.SESSION_TRANSACTED);
            } catch (JMSException e) {
                e.printStackTrace();
            }

            try {
                paymentInfoMapper.updateByExample(paymentInfo, example);
                //支付成功后，引起的系统服务-》订单服务更新-》库存服务-》物流
                //调用MQ，发送支付成功的消息
                Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
                MessageProducer producer = session.createProducer(payment_success_queue);

                //ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();//字符串文本
                ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();//hash结构
                activeMQMapMessage.setString("out_trade_no", paymentInfo.getOrderSn());
                producer.send(activeMQMapMessage);
                session.commit();
            } catch (Exception e) {
                //消息回滚
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
            } finally {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Override
    public void sendDelayPaymentResultCheckQueue(String tradeNum, Integer count) {

        Connection connection = null;
        Session session = null;

        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            Queue payment_check_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_check_queue);

            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", tradeNum);
            mapMessage.setInt("count", count);

            //为消息加入延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 30 * 1000);

            producer.send(mapMessage);
            session.commit();
        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {

        Map<String, Object> resultMap = new HashMap<>();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no", out_trade_no);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            resultMap.put("out_trade_no", response.getOutTradeNo());
            resultMap.put("trade_no", response.getTradeNo());
            resultMap.put("trade_status", response.getTradeStatus());
            requestMap.put("call_back_content", response.getMsg());
        } else {
            System.out.println("调用失败");
        }


        return resultMap;
    }
}
