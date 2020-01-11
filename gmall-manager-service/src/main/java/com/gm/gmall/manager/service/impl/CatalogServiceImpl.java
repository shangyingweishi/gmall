package com.gm.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.gm.gmall.bean.PmsBaseCatalog1;
import com.gm.gmall.bean.PmsBaseCatalog2;
import com.gm.gmall.bean.PmsBaseCatalog3;
import com.gm.gmall.manager.mapper.PmsBaseAttrInfoMapper;
import com.gm.gmall.manager.mapper.PmsBaseCatalog1Mapper;
import com.gm.gmall.manager.mapper.PmsBaseCatalog2Mapper;
import com.gm.gmall.manager.mapper.PmsBaseCatalog3Mapper;
import com.gm.gmall.service.CatalogWebService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogWebService {

    @Autowired
    PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;
    @Autowired
    PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;
    @Autowired
    PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;
    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;


    @Override
    public List<PmsBaseCatalog1> getCatalog1s() {
        List<PmsBaseCatalog1> catalog1s = pmsBaseCatalog1Mapper.selectAll();
        return catalog1s;
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2s(String catalog1Id) {
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);
        List<PmsBaseCatalog2> catalog2s = pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);

        return catalog2s;
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3s(String catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        List<PmsBaseCatalog3> catalog3s = pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);
        return catalog3s;
    }




}
