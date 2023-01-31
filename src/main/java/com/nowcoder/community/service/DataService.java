package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 将指定IP计入UV
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        List<String> keyList = new ArrayList<>();//存放日期范围内的每日uv的rediskey

        Calendar calendar = Calendar.getInstance();//为了计算日期
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String uvKey = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(uvKey);

            calendar.add(Calendar.DATE, 1); //加1天
        }

        //合并这些数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        //返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户计入DAU
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true); //以userId为索引存boolean值
    }

    // 统计指定日期范围内的DAU
    public long calculateDAU(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        List<byte[]> keyList = new ArrayList<>();//存放日期范围内的每日DAU的rediskey
        // 为什么key是byte[]类型？
        // 给定一个redisDAUKey，它一开始存储一个全为0的二进制串，每记录一个用户到DAU，就以userId为offset把该二进制串上该位置的0换成1

        Calendar calendar = Calendar.getInstance();//为了计算日期
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());

            calendar.add(Calendar.DATE, 1); //加1天
        }

        //进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
