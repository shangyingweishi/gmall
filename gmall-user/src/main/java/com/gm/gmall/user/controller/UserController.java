package com.gm.gmall.user.controller;

import com.gm.gmall.bean.UmsMember;
import com.gm.gmall.bean.UmsMemberReceiveAddress;
import com.gm.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("/getReceiveAddressByUserID")
    public List<UmsMemberReceiveAddress> getReceiveAddressByUserID(String memberId){

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getReceiveAddressByUserID(memberId);
        return umsMemberReceiveAddresses;
    }

    @RequestMapping("/getAllUser")
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMembers = userService.getAllUser();
        return  umsMembers;
    }


    @RequestMapping("/hello")
    public String hello(){
        return "hello user";
    }

}
