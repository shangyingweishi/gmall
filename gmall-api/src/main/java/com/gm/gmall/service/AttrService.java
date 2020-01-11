package com.gm.gmall.service;

import com.gm.gmall.bean.PmsBaseAttrInfo;
import com.gm.gmall.bean.PmsBaseAttrValue;
import com.gm.gmall.bean.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

public interface AttrService {

    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);


    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrValueListByAttrId(Set<String> valueSet);
}
