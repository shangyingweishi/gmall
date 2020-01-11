package com.gm.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gm.gmall.bean.PmsSkuAttrValue;
import com.gm.gmall.bean.PmsSkuImage;
import com.gm.gmall.bean.PmsSkuInfo;
import com.gm.gmall.bean.PmsSkuSaleAttrValue;
import com.gm.gmall.manager.mapper.PmsSkuAttrValueMapper;
import com.gm.gmall.manager.mapper.PmsSkuImageMapper;
import com.gm.gmall.manager.mapper.PmsSkuInfoMapper;
import com.gm.gmall.manager.mapper.PmsSkuSaleAttrValueMapper;
import com.gm.gmall.service.SkuService;
import com.gm.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        //插入spuInfo
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        //插入平台属性关联
        List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValues) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        //插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }


        //插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

    }

    public PmsSkuInfo getSkuByIdFromDb(String skuId){
        //sku商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImageList = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfo1.setSkuImageList(pmsSkuImageList);
        return pmsSkuInfo1;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId, String remoteAddr) {

        System.out.println("ip:" + remoteAddr + "---" + Thread.currentThread().getName() + "进入了商品详情页面");

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        //链接缓存
        Jedis jedis = redisUtil.getJedis();

        //查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuLock = "sku:" + skuId + ":lock";
        String skuJson = jedis.get(skuKey);
        if(StringUtils.isNotBlank(skuJson)){ //if(skuJson != null && !skuJson.equals(""))
            System.out.println("ip:" + remoteAddr + "---" + Thread.currentThread().getName() + "从缓存中获取了商品详情");

            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        }else {

            System.out.println("ip:" + remoteAddr + "---" + Thread.currentThread().getName() + "缓存中没有，申请缓存的分布式锁" + skuLock);

            //如果缓存中没有，查询mysql
            //设置分布式锁
            String uuid = UUID.randomUUID().toString();//设置一个UUID，防止删除了别的线程的锁
            String lock = jedis.set(skuLock, uuid, "nx", "px", 10000);//拿到锁的线程有10s过期时间
            if (StringUtils.isNotBlank(lock) /*&& nx.equals("ok")*/){

                System.out.println("ip:" + remoteAddr + "---" + Thread.currentThread().getName() + "有权在10s内访问数据库" + skuLock);

                //设置成功有权在10秒的过期时间内访问数据库
                pmsSkuInfo = getSkuByIdFromDb(skuId);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (pmsSkuInfo != null){
                    //查询结果存入redis
                    jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));
                }else {
                    //数据库中不存在该缓存
                    //为了防止缓存穿透，将null值或者空字符串设置给redis
                    jedis.setex(skuKey, 60*3, JSON.toJSONString(""));
                }

                System.out.println("ip:" + remoteAddr + "---" + Thread.currentThread().getName() + "使用完毕，将锁归还" + skuLock);
                //在访问mysql后，将mysql的分布式锁释放
                String lockUUID = jedis.get(skuLock);
                if (StringUtils.isNotBlank(lockUUID) && lockUUID.equals(uuid)){
                    jedis.del(skuLock);//用uuid确认是自己的sku的锁
                }


            }else {
                //设置失败，自旋(该线程在睡眠几秒后，重新尝试访问本方法)
                System.out.println("ip:" + remoteAddr + "---" + Thread.currentThread().getName() + "没拿到锁，开始自旋" );
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId, remoteAddr);
            }



        }



        jedis.close();

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySku(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySku(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            String skuId = pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfoList;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {

        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        BigDecimal price = pmsSkuInfo1.getPrice();

        if (price.compareTo(productPrice) == 0){
            b = true;
        }

        return b;
    }
}
