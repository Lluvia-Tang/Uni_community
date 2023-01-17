package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":"; //声明命名中一个分割的常量
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; //实体赞的前缀

    // 生成某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

}
