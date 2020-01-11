package com.gm.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gm.gmall.bean.PmsBaseAttrValue;
import com.gm.gmall.bean.PmsBaseSaleAttr;
import com.gm.gmall.service.AttrService;
import com.gm.gmall.bean.PmsBaseAttrInfo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class AttrController {

    @Reference
    AttrService attrService;

    @RequestMapping("/saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){
        String success = attrService.saveAttrInfo(pmsBaseAttrInfo);
        return "success";
    }

    @RequestMapping("/attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){

        List<PmsBaseAttrInfo> attrInfos = attrService.attrInfoList(catalog3Id);
        return attrInfos;

    }

    @RequestMapping("/getAttrValueList")
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){

        List<PmsBaseAttrValue> pmsBaseAttrValues = attrService.getAttrValueList(attrId);
        return pmsBaseAttrValues;

    }

    @RequestMapping("/baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = attrService.baseSaleAttrList();
        return pmsBaseSaleAttrs;
    }

}
