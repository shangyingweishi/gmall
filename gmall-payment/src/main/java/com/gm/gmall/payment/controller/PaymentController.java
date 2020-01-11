package com.gm.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.gm.gmall.annotations.LoginRequired;
import com.gm.gmall.bean.OmsOrder;
import com.gm.gmall.bean.PaymentInfo;
import com.gm.gmall.payment.config.AlipayConfig;
import com.gm.gmall.service.OrderService;
import com.gm.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;
    @Autowired
    PaymentService paymentService;
    @Reference
    OrderService orderService;

    @RequestMapping("/index")
    @LoginRequired(loginSuccess = true)
    public String index(String tradeNum,String memberId,String nickname, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){


        modelMap.put("tradeNum", tradeNum);
        modelMap.put("totalAmount", totalAmount);
        modelMap.put("nickname", nickname);

        return "index";
    }

    //微信支付
    @RequestMapping("/mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(String tradeNum,String memberId,String nickname, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){

        return "";

    }

    //支付宝支付
    @RequestMapping("/alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String tradeNum,String memberId,String nickname, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){

        modelMap.put("tradeNum", tradeNum);
        modelMap.put("totalAmount", totalAmount);

        //获得一个支付宝请求的客户端(不是链接，是一个封装号的http的表单请求)
        String form = null;
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();//创建API对应的request

        //回调函数
        payRequest.setReturnUrl(AlipayConfig.return_payment_url);
        payRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no", tradeNum);
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.01);
        map.put("subject", "商品信息");
        String param = JSON.toJSONString(map);
        payRequest.setBizContent(param);

        try {
            form = alipayClient.pageExecute(payRequest).getBody();//调用SDK生成表单
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //生成并保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderByTradeNum(tradeNum);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(tradeNum);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("商品名称信息");
        paymentInfo.setTotalAmount(totalAmount);

        paymentService.savePaymentInfo(paymentInfo);

        //向消息中间件发送一个检查支付状态(支付服务消费)的延迟消息队列
        paymentService.sendDelayPaymentResultCheckQueue(tradeNum, 6);

        //提交请求到支付宝
        return form;

    }

    @RequestMapping("/alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String aliCallBackReturn(HttpServletRequest request, ModelMap modelMap){

        //回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();



        //通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求无法验签
        if(StringUtils.isNotBlank(sign)){
            //验签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);//支付宝的交易凭证
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());

            //更新用户支付状态
            paymentService.updatePayment(paymentInfo);
        }

        //支付成功后，引起的系统服务-》订单服务更新-》库存服务-》物流
        //调用MQ，发送支付成功的消息


        return "finish";

    }

}
