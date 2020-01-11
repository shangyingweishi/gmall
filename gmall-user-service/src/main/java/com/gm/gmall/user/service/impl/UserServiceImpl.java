package com.gm.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gm.gmall.bean.UmsMember;
import com.gm.gmall.bean.UmsMemberReceiveAddress;
import com.gm.gmall.service.UserService;
import com.gm.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.gm.gmall.user.mapper.UserMapper;
import com.gm.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    UmsMember umsMember = new UmsMember();
    UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = userMapper.selectAll();
        return umsMembers;
    }


    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByUserID(String memberId) {
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> addresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return addresses;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();

            if (jedis != null){
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + umsMember.getUsername() + ":info");

                if (StringUtils.isNotBlank(umsMemberStr)) {
                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                } /*else {
                    //密码错误
                    //或者缓存中没有，链接数据库
                     UmsMember umsMemberFromDb = loginFromDb(umsMember);
                    if (umsMemberFromDb != null){
                        jedis.setex("user:" + umsMember.getPassword() + ":info",60*60*24, JSON.toJSONString(umsMemberFromDb));
                    }
                    return umsMemberFromDb;

                }*/
            }/*else {
                //链接redis失败开启数据库
                 UmsMember umsMemberFromDb = loginFromDb(umsMember);
                    if (umsMemberFromDb != null){
                        jedis.setex("user:" + umsMember.getPassword() + ":info",60*60*24, JSON.toJSONString(umsMemberFromDb));
                    }
                    return umsMemberFromDb;

            }*/

            //链接redis失败,开启数据库
            //提取相同代码
            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if (umsMemberFromDb != null){
                jedis.setex("user:" + umsMember.getPassword() + umsMember.getUsername() + ":info",60*60*24, JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;

        } finally {
            jedis.close();
        }

    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            jedis.setex("user:" + memberId + ":token", 60*60*2, token);
        }finally {
            jedis.close();
        }
    }

    @Override
    public UmsMember addOauthUSER(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember checkOauthUser(UmsMember userCheck) {

        UmsMember umsMember = userMapper.selectOne(userCheck);

        return umsMember;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressByID(String receiveAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }

    private UmsMember loginFromDb(UmsMember umsMember) {

        List<UmsMember> umsMembers = userMapper.select(umsMember);
        if (umsMembers != null){
            return umsMembers.get(0);
        }

        return null;

    }
}
