package com.gm.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gm.gmall.bean.PmsBaseCatalog1;
import com.gm.gmall.bean.PmsBaseCatalog2;
import com.gm.gmall.bean.PmsBaseCatalog3;
import com.gm.gmall.service.CatalogWebService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class CatalogController {

    @Reference
    CatalogWebService catalogWebService;

    @RequestMapping("/getCatalog1")
    public List<PmsBaseCatalog1> getCatalog1(){
        List<PmsBaseCatalog1> catalog1s = catalogWebService.getCatalog1s();
        return  catalog1s;
    }

    @RequestMapping("/getCatalog2")
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id){
        return catalogWebService.getCatalog2s(catalog1Id);
    }

    @RequestMapping("/getCatalog3")
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){
        return catalogWebService.getCatalog3s(catalog2Id);
    }

}
