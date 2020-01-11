package com.gm.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gm.gmall.bean.PmsSearchSkuInfo;
import com.gm.gmall.bean.PmsSkuInfo;
import com.gm.gmall.bean.PmsSkuSaleAttrValue;
import com.gm.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;//查询mysql

    @Autowired
    JestClient jestClient;


    @Test
    public void test() throws IOException {

        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //query
        searchSourceBuilder.query(boolQueryBuilder);

        //filter
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","39");
        boolQueryBuilder.filter(termQueryBuilder);
        //must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","荣耀");
        boolQueryBuilder.must(matchQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        searchSourceBuilder.highlight(null);

        String dslStr = searchSourceBuilder.toString();

        System.err.println(dslStr);

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        //用api执行复杂查询
        Search build = new Search.Builder(dslStr).addIndex("gmallpms").addType("PmsSkuInfo").build();

        SearchResult execute = jestClient.execute(build);

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            pmsSearchSkuInfos.add(source);
        }
        System.out.println(pmsSearchSkuInfos.size());
    }

    @Test
    public void contextLoads() throws IOException {

        //查询mysql数据
        List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();

        pmsSkuInfoList = skuService.getAllSku("61");

        //转换成es数据
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        }

        //导入es
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            Index build = new Index.Builder(pmsSearchSkuInfo).index("gmallpms").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(build);

        }

    }

}
