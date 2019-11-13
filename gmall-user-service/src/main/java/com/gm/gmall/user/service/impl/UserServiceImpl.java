package com.gm.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.gm.gmall.bean.UmsMember;
import com.gm.gmall.bean.UmsMemberReceiveAddress;
import com.gm.gmall.service.UserService;
import com.gm.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.gm.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

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
}
