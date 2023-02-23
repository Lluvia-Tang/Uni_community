package com.nowcoder.community.controller;
/**
 * Redis集群实例
 */

import com.nowcoder.community.service.RedisClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

//@Controller
public class RedisClusterController {
    @Autowired
    private RedisClusterService redisClusterService;

    @RequestMapping("/setRedis")
    @ResponseBody
    public String setRedis(String key, String value) {
        redisClusterService.setStr(key, value);
        return "success";
    }

    @RequestMapping("/getKey")
    @ResponseBody
    public Object getKey(String key){
        Object result = redisClusterService.getKey(key);
        return result == null ? "缓存中没有该数据" : result;
    }

    @RequestMapping("/delStringKey")
    @ResponseBody
    public String reStrRedis(String key){
        redisClusterService.delKey(key);
        return "success";
    }
}