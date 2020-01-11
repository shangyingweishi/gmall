package com.gm.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gm.gmall.annotations.LoginRequired;
import com.gm.gmall.bean.OmsCartItem;
import com.gm.gmall.bean.OmsOrder;
import com.gm.gmall.bean.OmsOrderItem;
import com.gm.gmall.bean.UmsMemberReceiveAddress;
import com.gm.gmall.service.CartService;
import com.gm.gmall.service.OrderService;
import com.gm.gmall.service.SkuService;
import com.gm.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;
    @Reference
    UserService userService;
    @Reference
    OrderService orderService;
    @Reference
    SkuService skuService;

    @RequestMapping("/submitOrder")
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //检查交易码
        String success = orderService.checkTradeCode(memberId, tradeCode);

        if (success.equals("success")){


            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            OmsOrder omsOrder = new OmsOrder();

            String tradeNum = "gmall";
            tradeNum = tradeNum + System.currentTimeMillis();//将毫秒时间戳拼接到订单号后
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            tradeNum = tradeNum + simpleDateFormat.format(new Date());//将时间字符串拼接到外部订单号

            //设置自动收货时间
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setOrderSn(tradeNum);
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressByID(receiveAddressId);
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());

            //时间工具类
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);//当前日期加一天
            omsOrder.setReceiveTime(calendar.getTime());

            omsOrder.setSourceType(0);
            omsOrder.setStatus(1);
            omsOrder.setTotalAmount(totalAmount);

            //根据用户id获得要购买的商品列表（购物车）以及总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            for (OmsCartItem omsCartItem : omsCartItems) {
                if ((omsCartItem.getIsChecked().equals("1"))){
                    //获取订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();

                    //验价
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(), omsCartItem.getPrice());
                    if (b == false){
                        ModelAndView modelAndView = new ModelAndView("tradeFail");
                        return modelAndView;
                    }
                    //验库存,远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());

                    omsOrderItem.setOrderSn(tradeNum);
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("123456789");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn(omsCartItem.getProductSkuId());//仓库中的skuId

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);



            //将订单和订单详情写入数据库
            //删除购物车对应的商品
            orderService.saveOrder(omsOrder);

            //重定向到支付系统
            ModelAndView modelAndView = new ModelAndView("redirect:http://localhost:8087/index");
            modelAndView.addObject("memberId", memberId);
            modelAndView.addObject("nickname", nickname);
            modelAndView.addObject("tradeNum", tradeNum);
            modelAndView.addObject("totalAmount", totalAmount);

            return modelAndView;

        }else {
            ModelAndView modelAndView = new ModelAndView("tradeFail");
            return modelAndView;
        }

    }

    @RequestMapping("/toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //收件人地址列表
        List<UmsMemberReceiveAddress> receiveAddressByUserID = userService.getReceiveAddressByUserID(memberId);
        modelMap.put("receiveAddressByUserID", receiveAddressByUserID);


        //获取购物车集合,将购物车集合转化为页面计算清单集合
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            //每循环一个购物车对象，就封装一个商品的详情到OmsOrderItem
            //判断是否商品是否选中
            if (omsCartItem.getIsChecked().equals("1")){
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItems.add(omsOrderItem);
            }
        }

        modelMap.put("omsOrderItems", omsOrderItems);
        modelMap.put("totalAmount",getTotalAmount(omsCartItems));

        //生成交易码，为了在提交订单时做交易码的校验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode",tradeCode);

        return "trade";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItemList) {
        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItemList) {
            if (omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
            }
        }

        return totalAmount;
    }

}
