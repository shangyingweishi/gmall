package com.gm.gmall.service;

import com.gm.gmall.bean.UmsMember;
import com.gm.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByUserID(String id);
}
