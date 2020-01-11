package com.gm.gmall.service;

import com.gm.gmall.bean.UmsMember;
import com.gm.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByUserID(String id);

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String memberId);

    UmsMember addOauthUSER(UmsMember umsMember);

    UmsMember checkOauthUser(UmsMember userCheck);

    UmsMemberReceiveAddress getReceiveAddressByID(String receiveAddressId);
}
