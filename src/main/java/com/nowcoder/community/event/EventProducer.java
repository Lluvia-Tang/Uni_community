package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * 开发事件的生产者
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //重发时间间隔
//    private static final long RETRY_INTERVAL = 10000L;

    //最大重试次数
//    private static final int MAX_RETRY_TIMES = 3;

    // 使用线程安全的队列保存未成功发送的消息
    private ConcurrentLinkedQueue<Event> failedEvents = new ConcurrentLinkedQueue<>();

    //处理事件
    public void fireEvnet(Event event){
        // 将事件发布到指定的主题topic
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event)); //将event对象封装成json字符串

        // 保证生产者端可靠传输
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                // 发送成功，不做处理
                System.out.println("Send message success: " + result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                // 发送失败，将未成功发送的消息添加到队列中
                System.out.println("Send message failed: " + ex.getMessage());
                failedEvents.offer(event);
            }
        });
    }

    // 获取未成功发送的消息队列
    public ConcurrentLinkedQueue<Event> getFailedEvents() {
        return failedEvents;
    }

    // 定时任务，定期检查未成功发送的消息，并进行重发
    // 使用quartz实现
//    @Scheduled(fixedRate = 3000) // 每3秒执行一次
//    public void retryFailedEvents() {
//        while (!failedEvents.isEmpty()) {
//            Event event = failedEvents.poll(); // 从队列中取出一条消息进行重发
//            fireEvnet(event);
//        }
//    }
}
