package com.gm.gmall.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwt {

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("memberId","1");
        map.put("nickName","zhangsan");
        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String encode = JwtUtil.encode("2019gmall", map, ip + time);
        System.err.println(encode);
    }

}
