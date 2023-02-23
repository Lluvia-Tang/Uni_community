package com.nowcoder.community.service;
/**
 * Redis工具类
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

//@Service
public class RedisClusterService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void setStr(String key, String value) {
        setStr(key, value, null);
    }

    public void setStr(String key, String value, Long time) {
        /*stringRedisTemplate.opsForValue().set(key, value);
        if (time != null)
            stringRedisTemplate.expire(key, time, TimeUnit.SECONDS);*/
        stringRedisTemplate.opsForValue().set(key,value);
        if(time != null){
            stringRedisTemplate.expire(key, time, TimeUnit.SECONDS);
        }
        /*if(time != null){
            jedisCluster.expire(key,(int) (time/1000));
        }*/
    }

    public Object getKey(String key) {
        /*return redisTemplate.opsForValue().get(key);*/
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delKey(String key) {
        /*stringRedisTemplate.delete(key);*/
        stringRedisTemplate.delete(key);
    }

}
