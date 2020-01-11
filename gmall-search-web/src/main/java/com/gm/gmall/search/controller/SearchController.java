package com.gm.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gm.gmall.annotations.LoginRequired;
import com.gm.gmall.bean.*;
import com.gm.gmall.service.AttrService;
import com.gm.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.*;

@Controller
@CrossOrigin
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("/index")
    @LoginRequired(loginSuccess = false)
    public String index() {
        return "index";
    }

    @RequestMapping("/list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {//三级分类id，关键字

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = null;
        try {
            //调用搜索服务，返回搜索结果
            pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }

        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        //抽取检索结果所包含的平台属性集合
        Set<String> valueSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueSet.add(valueId);
            }
        }
        //根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByAttrId(valueSet);
        modelMap.put("attrList", pmsBaseAttrInfos);

        //对平台属性集合进一步处理，去掉当前条件中valueId所在属性组,用迭代器操作
        String[] delValueIds = pmsSearchParam.getValueId();
        if (delValueIds != null) {
            //如果valueId的参数不为空，说明当前请求中包含属性的参数，每一个属性参数，都是一个面包屑
            //面包屑功能
            String keyword = pmsSearchParam.getKeyword();
            if (StringUtils.isNotBlank(keyword)) {
                modelMap.put("keyword", keyword);
            }
            List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();

            for (String delValueId : delValueIds) {
                Iterator<PmsBaseAttrInfo> pmsBaseAttrInfoIterator = pmsBaseAttrInfos.iterator();//平台属性集合
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam, delValueId));

                while (pmsBaseAttrInfoIterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = pmsBaseAttrInfoIterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();

                        if (delValueId.equals(valueId)) {
                            //查找面包屑的属性名名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            pmsBaseAttrInfoIterator.remove();
                        }
                    }

                }
                pmsSearchCrumbList.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbList);

        }

        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);

       /* //面包屑功能
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);
        }*//*
        List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();*/
        /*if (delValueIds != null) {
            //如果valueId的参数不为空，说明当前请求中包含属性的参数，每一个属性参数，都是一个面包屑
            for (String delValueId : delValueIds) {
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setValueName(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam, delValueId));
                pmsSearchCrumbList.add(pmsSearchCrumb);
            }
        }*/


        return "list";
    }

    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam, String delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String urlParam = "";


        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }


        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }


        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if (!pmsSkuAttrValue.equals(delValueId)) {
                    urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
                }

            }
        }

        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam, String... delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String urlParam = "";


        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }


        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }


        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
            }
        }

        return urlParam;
    }

}
