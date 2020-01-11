package com.gm.gmall.service;

import com.gm.gmall.bean.PmsProductImage;
import com.gm.gmall.bean.PmsProductInfo;
import com.gm.gmall.bean.PmsProductSaleAttr;
import com.gm.gmall.bean.PmsSkuAttrValue;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);

    void saveSpuInfo(PmsProductInfo pmsProductInfo);


    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId);
}
