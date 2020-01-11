package com.gm.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gm.gmall.bean.*;
import com.gm.gmall.manager.util.PmsUploadUtil;
import com.gm.gmall.service.SpuService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("/spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
         List<PmsProductSaleAttr> pmsProductSaleAttrList =  spuService.spuSaleAttrList(spuId);
         return pmsProductSaleAttrList;
    }

    @RequestMapping("/spuImageList")
    public List<PmsProductImage> spuImageList(String spuId){
        List<PmsProductImage> pmsProductImages =  spuService.spuImageList(spuId);
        return pmsProductImages;
    }

    //商品列表
    @RequestMapping("/spuList")
    public List<PmsProductInfo> spuList(String catalog3Id){

        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    //保存商品信息
    @RequestMapping("/saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){

        spuService.saveSpuInfo(pmsProductInfo);

        return "success";

    }

    //图片上传
    @RequestMapping("/fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        //将图片或者音视频上传到分布式的文件存储系统

        //将图片的存储路径返回给页面
        String imgUrl = PmsUploadUtil.fileUpload(multipartFile);

        System.out.println(imgUrl);
        return imgUrl;

    }

}
