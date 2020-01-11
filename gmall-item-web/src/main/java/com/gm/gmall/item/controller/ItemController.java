package com.gm.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gm.gmall.bean.PmsBaseAttrInfo;
import com.gm.gmall.bean.PmsProductSaleAttr;
import com.gm.gmall.bean.PmsSkuInfo;
import com.gm.gmall.bean.PmsSkuSaleAttrValue;
import com.gm.gmall.service.SkuService;
import com.gm.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;



    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap, HttpServletRequest request){

        String remoteAddr = request.getRemoteAddr();

        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId, remoteAddr);
        //sku对象
        modelMap.put("skuInfo", pmsSkuInfo);

        //销售属性列表
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), pmsSkuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

        //查询当前spu的sku的其他sku集合的hash表
        HashMap<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySku(pmsSkuInfo.getProductId());
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String v = skuInfo.getId();
            String k = "";
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";

            }
            skuSaleAttrHash.put(k, v);
        }

        //将sku的销售属性hash表放到页面
        String skuSaleAttrHashToJSONStr = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrHashToJSONStr", skuSaleAttrHashToJSONStr);


        return "item";
    }


    @RequestMapping("/test")
    public String test(ModelMap modelMap){

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("循环数据" + i);
        }

        modelMap.put("check", 1);
        modelMap.put("list", list);
        modelMap.put("hello","hello thymeleaf!!!");
        return "index";
    }

}
