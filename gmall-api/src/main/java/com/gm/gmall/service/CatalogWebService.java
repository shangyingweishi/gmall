package com.gm.gmall.service;

import com.gm.gmall.bean.PmsBaseAttrInfo;
import com.gm.gmall.bean.PmsBaseCatalog1;
import com.gm.gmall.bean.PmsBaseCatalog2;
import com.gm.gmall.bean.PmsBaseCatalog3;

import java.util.List;

public interface CatalogWebService {

    List<PmsBaseCatalog1> getCatalog1s();

    List<PmsBaseCatalog2> getCatalog2s(String catalog1Id);

    List<PmsBaseCatalog3> getCatalog3s(String catalog2Id);

}
