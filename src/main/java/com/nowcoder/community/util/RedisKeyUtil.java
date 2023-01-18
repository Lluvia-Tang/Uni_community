package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":"; //声明命名中一个分割的常量
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; //实体赞的前缀
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee"; //统计关注目标
    private static final String PREFIX_FOLLOWER = "follower"; //统计粉丝

    // 生成某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体
    // followee: userId: entityType -> zset(entityId, nowTime)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的粉丝
    //follower: entityType: entityId -> zset(userId, nowTime)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER +SPLIT + entityType + SPLIT + entityId;
    }
}
