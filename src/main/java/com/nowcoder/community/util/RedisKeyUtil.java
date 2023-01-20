package com.nowcoder.community.util;

import com.alibaba.druid.support.spring.stat.annotation.Stat;

public class RedisKeyUtil {

    private static final String SPLIT = ":"; //声明命名中一个分割的常量
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; //实体赞的前缀
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee"; //统计关注目标
    private static final String PREFIX_FOLLOWER = "follower"; //统计粉丝
    private static final String PREFIX_KAPTCHA = "kaptcha"; //验证码
    private static final String PREFIX_TICKET = "ticket"; //登录凭证
    private static final String PREFIX_USER = "user"; //用户信息

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

    //登录验证码
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_USER +SPLIT + userId;
    }
}
