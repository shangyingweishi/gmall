package com.gm.gmall.order.mq;

import com.gm.gmall.bean.OmsOrder;
import com.gm.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderServiceMqListener {
    
    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage){

        String outTradeNo = "";
        try {
            outTradeNo = mapMessage.getString("out_trade_no");
            System.out.println(outTradeNo);

            //更新订单状态业务
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOrderSn(outTradeNo);
            orderService.updateOrder(omsOrder);
            System.out.println("123");

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

}
