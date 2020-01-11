package com.gm.gmall.service;

import com.gm.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem ifCartExixtByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFromDb);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);
}
