package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 开发事件的生产者
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //处理事件
    public void fireEvnet(Event event){
        // 将事件发布到指定的主题topic
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event)); //将event对象封装成json字符串
    }
}
