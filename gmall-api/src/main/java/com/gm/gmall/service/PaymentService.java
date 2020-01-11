package com.gm.gmall.service;

import com.gm.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheckQueue(String tradeNum, Integer count);

    Map<String, Object> checkAlipayPayment(String out_trade_no);
}
