package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate; //数据放入redis


    //点赞(两次更新Redis)(一项业务有两次存储==>开启事务)
    public void like(int userId, int entityType, int entityId, int entityUserId){
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId); //redisKey
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);//判断是否已经点过赞
//        if (isMember){
//            //点过赞
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            //没点过赞
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        // 在一个连接里执行多条操作
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                //查询要在事务之外，不然Redis不会立即执行
                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);//判断是否已经点过赞

                //开启事务
                operations.multi();

                if (isMember){
                    //取消点赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    //点赞
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });

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

    // 查询某用户获得的赞的数量
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null? 0 : count;
    }
}
