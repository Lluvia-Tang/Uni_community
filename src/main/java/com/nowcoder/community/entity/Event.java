package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 封装事件
 */
public class Event {

    private String topic;
    private int userId; //触发事件者
    private int entityType;
    private int entityId;
    private int entityUserId; //事件接受者
    private Map<String, Object> data = new HashMap<>(); //可扩展性，其他额外的数据存放至map中

    //改造set方法，使可以连续set不同的属性
    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
