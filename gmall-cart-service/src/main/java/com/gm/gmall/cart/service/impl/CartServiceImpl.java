package com.gm.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gm.gmall.bean.OmsCartItem;
import com.gm.gmall.cart.mapper.OmsCartItemMapper;
import com.gm.gmall.service.CartService;
import com.gm.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Override
    public OmsCartItem ifCartExixtByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);

        return omsCartItemMapper.selectOne(omsCartItem);
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {

        if(StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);//insertSelective避免添加空值
        }


    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id", omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExample(omsCartItemFromDb, e);

    }

    @Override
    public void flushCartCache(String memberId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);

        //同步到redis缓存中
        Jedis jedis = redisUtil.getJedis();

        Map<String,String> map = new HashMap<>();
        for (OmsCartItem cartItem : omsCartItems) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }

        jedis.del("user:" + memberId + ":cart");
        jedis.hmset("user:" + memberId + ":cart", map);

        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {

        Jedis jedis =null;
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItemList.add(omsCartItem);

            }
        }catch (Exception e){
            //处理异常，记录系统日志
            e.printStackTrace();
//            String message = e.getMessage();
//            logService.addErrLog(message);
            return null;
        }finally {
            jedis.close();
        }

        return omsCartItemList;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productSkuId", omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);

        //缓存同步
        flushCartCache(omsCartItem.getMemberId());
    }


}
