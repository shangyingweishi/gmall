package com.gm.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.gm.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {

    public String getCode(){
        String s = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=1780125040&response_type=code&redirect_uri=http://192.168.56.1:8085/vlogin");

        System.out.println(s);
        return null;
    }

    public String getAccessToken(){
        Map<String,String> map = new HashMap<>();
        map.put("client_id","1780125040");
        map.put("client_secret","bc293263c078ef9a4408a6a0a458fe33");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://192.168.56.1:8085/vlogin");
        map.put("code","8be3016279b89439f49bbe754c50ee64");

        String s1 = "https://api.weibo.com/oauth2/access_token";
        String access_token_json = HttpclientUtil.doPost(s1, map);
        Map<String,String> access_token = JSON.parseObject(access_token_json,Map.class);
        System.out.println(access_token.get("access_token"));//多次获得code不一样，但是access_token是一样的
        return access_token.get("access_token");
    }

    public Map<String,String> getUserInfo(){
        String s2 = "https://api.weibo.com/2/users/show.json?access_token=2.00MllAcFMqNTwB5b4a7716830K2ONm&uid=5142348522";
        String user_json = HttpclientUtil.doGet(s2);
        Map<String,String> user_map = JSON.parseObject(user_json,Map.class);
        return user_map;
    }

    public static void main(String[] args) {
//        App Key：1780125040
//        App Secret：bc293263c078ef9a4408a6a0a458fe33
//        http://192.168.56.1:8085/vlogin

       /* String s = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=1780125040&response_type=code&redirect_uri=http://192.168.56.1:8085/vlogin");

        System.out.println(s);*/

        //http://192.168.56.1:8085/vlogin?code=8be3016279b89439f49bbe754c50ee64

//        String s1 = "https://api.weibo.com/oauth2/access_token" ;
//                "?client_id=1780125040&client_secret=bc293263c078ef9a4408a6a0a458fe33&grant_type=authorization_code&redirect_uri=" +
//                "http://192.168.56.1:8085/vlogin&code=8be3016279b89439f49bbe754c50ee64"

//        Map<String,String> map = new HashMap<>();
//        map.put("client_id","1780125040");
//        map.put("client_secret","bc293263c078ef9a4408a6a0a458fe33");
//        map.put("grant_type","authorization_code");
//        map.put("redirect_uri","http://192.168.56.1:8085/vlogin");
//        map.put("code","8be3016279b89439f49bbe754c50ee64");

//        String access_token_json = HttpclientUtil.doPost(s1, map);
//        Map<String,String> access_token = JSON.parseObject(access_token_json,Map.class);
//        System.out.println(access_token.get("access_token"));//多次获得code不一样，但是access_token是一样的

        //{"access_token":"2.00MllAcFMqNTwB5b4a7716830K2ONm","remind_in":"157679999","expires_in":157679999,"uid":"5142348522","isRealName":"true"}

        //用access_token查询用户信息
//        String s2 = "https://api.weibo.com/2/users/show.json?access_token=2.00MllAcFMqNTwB5b4a7716830K2ONm&uid=5142348522";
//        String user_json = HttpclientUtil.doGet(s2);
//        Map<String,String> user_map = JSON.parseObject(user_json,Map.class);
//        System.out.println(user_map);
    }

}
