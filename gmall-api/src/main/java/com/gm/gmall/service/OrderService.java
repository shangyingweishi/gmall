package com.gm.gmall.service;

import com.gm.gmall.bean.OmsOrder;

public interface OrderService {
    OmsOrder getOrderByTradeNum(String tradeNum);

    String checkTradeCode(String memberId, String tradeCode);

    String genTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    void updateOrder(OmsOrder omsOrder);
}
