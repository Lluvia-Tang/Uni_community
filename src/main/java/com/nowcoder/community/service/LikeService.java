package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate; //数据放入redis

    //点赞
    public void like(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId); //redisKey
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);//判断是否已经点过赞
        if (isMember){
            //点过赞
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            //没点过赞
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId); //redisKey
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某用户对某实体点赞的状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId); //redisKey
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
