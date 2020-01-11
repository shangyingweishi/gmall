package com.gm.gmall.service;

import com.gm.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId, String remoteAddr);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySku(String productId);

    List<PmsSkuInfo> getAllSku(String catalog3Id);

    boolean checkPrice(String productSkuId, BigDecimal productPrice);
}
