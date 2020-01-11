package com.gm.gmall.service;

import com.gm.gmall.bean.PmsSearchParam;
import com.gm.gmall.bean.PmsSearchSkuInfo;

import java.io.IOException;
import java.util.List;


public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) throws IOException;
}
