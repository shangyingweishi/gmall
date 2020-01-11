package com.gm.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gm.gmall.bean.UmsMember;
import com.gm.gmall.service.UserService;
import com.gm.gmall.util.HttpclientUtil;
import com.gm.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("/vlogin")
    public String vlogin(String code, HttpServletRequest request){

        //授权码换取access_token
        Map<String,String> map = new HashMap<>();
        map.put("client_id","1780125040");
        map.put("client_secret","bc293263c078ef9a4408a6a0a458fe33");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://192.168.56.1:8085/vlogin");
        map.put("code",code);

        String s1 = "https://api.weibo.com/oauth2/access_token";
        String access_token_json = HttpclientUtil.doPost(s1, map);
        Map<String,Object> access_map = JSON.parseObject(access_token_json,Map.class);//多次获得code不一样，但是access_token是一样的


        //access_token换取用户信息
        String uid = (String) access_map.get("uid");
        String access_token = (String) access_map.get("access_token");
        String url = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;
        String userInfoJson = HttpclientUtil.doGet(url);
        Map<String,Object> userInfoMap = JSON.parseObject(userInfoJson, Map.class);

        //将用户数据保存数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid(uid);
        umsMember.setSourceType(2);
        umsMember.setAccessCode(code);
        umsMember.setCity((String) userInfoMap.get("location"));

        String gender = (String) userInfoMap.get("gender");
        gender = (gender.equals("m")) ? "1" : "0";
        umsMember.setGender(Integer.parseInt(gender));

        umsMember.setNickname((String) userInfoMap.get("screen_name"));

        UmsMember userCheck = new UmsMember();
        userCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember checkOauthUser = userService.checkOauthUser(userCheck);//检查社交用户以前是否登录过系统

        if (checkOauthUser == null){
            umsMember = userService.addOauthUSER(umsMember);//添加社交用户
        }else {
            umsMember = checkOauthUser;
        }

        //生成jwt的token，并且重定向到首页，携带该token
        String token = null;
        String memberId = umsMember.getId();//rpc的主键返回策略失效，会导致空值
        String nickname = umsMember.getNickname();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId);
        userMap.put("nickname",nickname);

        String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();//从request中获取ip
        }
        if (StringUtils.isBlank(ip)){
            ip = "127.0.0.1";
        }

        //加密生成token
        token = JwtUtil.encode("2019gmall", userMap, ip);

        //将token存入redis中
        userService.addUserToken(token,memberId);

        return "redirect:http://localhost:8083/index?token=" + token;
    }

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token, String currentIp){

        //通过jwt校验token真假
        Map<String,String> map = new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall", currentIp);
        if (decode != null){

            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));
        }else {
            map.put("status", "fail");
        }

        return JSON.toJSONString(map);
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){

        String token = "";

        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin != null){
            //登陆成功

            //用jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId",memberId);
            userMap.put("nickname",nickname);

            String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
            if(StringUtils.isBlank(ip)){
               ip = request.getRemoteAddr();//从request中获取ip
            }
            if (StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }

            //加密生成token
           token = JwtUtil.encode("2019gmall", userMap, ip);

            //将token存入redis中
            userService.addUserToken(token,memberId);
        }else{
            //登陆失败
            token = "fail";
        }

        return token;
    }

    @RequestMapping("/index")
    public String index(String ReturnUrl, ModelMap modelMap){

        modelMap.put("ReturnUrl", ReturnUrl);

        return "index";
    }

}
