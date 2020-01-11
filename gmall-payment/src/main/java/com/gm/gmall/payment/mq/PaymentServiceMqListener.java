package com.gm.gmall.payment.mq;

import com.gm.gmall.bean.PaymentInfo;
import com.gm.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentCheckResult(MapMessage mapMessage) {

        String out_trade_no = "";
        int count;
        try {
            out_trade_no = mapMessage.getString("out_trade_no");
            count = mapMessage.getInt("count");
            //调用paymentService的支付宝检查接口
            System.out.println("延迟检查，调用支付检查的接口服务");
            Map<String, Object> mapResult = paymentService.checkAlipayPayment(out_trade_no);
            if (mapResult.isEmpty()) {
                if (count > 0) {
                    count--;
                    paymentService.sendDelayPaymentResultCheckQueue(out_trade_no, count);
                    System.out.println("没有支付成功，剩余检查次数：" + count + ",继续发送延迟检查任务");
                } else {
                    System.out.println("检查次数用尽，结束检查");
                }
            } else {

                String trade_status = (String) mapResult.get("trade_status");

                //根据查询的支付状态结果，判断是否进行下一次的延迟任务还是支付成功更新数据和后续任务的操作
                if (StringUtils.isNotBlank(trade_status) && trade_status.equals("TRADE_SUCCESS")) {
                    //支付成功，更新支付发送支付队列
                    //进行支付更新幂等性检查，防止重复更新(详见PaymentServiceImpl)

                    PaymentInfo paymentInfo = new PaymentInfo();
                    paymentInfo.setOrderSn(out_trade_no);
                    paymentInfo.setPaymentStatus("已支付");
                    paymentInfo.setAlipayTradeNo((String) mapResult.get("trade_no"));//支付宝的交易凭证
                    paymentInfo.setCallbackContent((String) mapResult.get("call_back_content"));//回调请求字符串
                    paymentInfo.setCallbackTime(new Date());

                    paymentService.updatePayment(paymentInfo);
                    System.out.println("支付成功，调用支付服务，修改支付信息，发送支付成功队列");
                } else {
                    //继续发送延迟检查任务，计算延迟时间等
                    if (count > 0) {
                        count--;
                        paymentService.sendDelayPaymentResultCheckQueue(out_trade_no, count);
                        System.out.println("没有支付成功，剩余检查次数：" + count + ",继续发送延迟检查任务");
                    } else {
                        System.out.println("检查次数用尽，结束检查");
                    }
                }
            }


        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
