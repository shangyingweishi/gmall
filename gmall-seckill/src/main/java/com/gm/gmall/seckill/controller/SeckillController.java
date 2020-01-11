package com.gm.gmall.seckill.controller;

import com.gm.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisUtil redisUtil;

    @RequestMapping("/secKill")
    @ResponseBody
    public String secKill(){

        Jedis jedis = redisUtil.getJedis();

        RSemaphore semaphore = redissonClient.getSemaphore("111");

        boolean b = semaphore.tryAcquire();

        int i = Integer.parseInt(jedis.get("111"));
        if (b){
            System.out.println("当前库存剩余数量：" + i + ",?用户抢购成功,当前抢购人数" + (1000 - i));

            //下一步用消息队列发出订单消息

        }else {
            System.out.println("当前库存剩余数量：" + i + ",用户抢购失败" );
        }

        jedis.close();

        return "1";
    }

    @RequestMapping("/kill")
    @ResponseBody
    public String kill(){

        Jedis jedis = redisUtil.getJedis();
        //开启商品监控
        jedis.watch("111");
        Integer i = Integer.parseInt(jedis.get("111"));
        if (i > 0) {
            Transaction multi = jedis.multi();

            multi.incrBy("111", -1);
            List<Object> exec = multi.exec();
            if (exec != null && exec.size() > 0){
                System.out.println("当前库存剩余数量：" + i + ",?用户抢购成功,当前抢购人数" + (1000 - i));
                //用消息队列发出订单消息
            }else {
                System.out.println("当前库存剩余数量：" + i + ",用户抢购失败" );
            }
        }

        jedis.close();

        return "1";
    }

}
