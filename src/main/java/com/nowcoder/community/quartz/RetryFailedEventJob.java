package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 消息队列中生产者端重发消息定时任务
 */
public class RetryFailedEventJob implements Job {

    @Autowired
    private EventProducer eventProducer;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        while (!eventProducer.getFailedEvents().isEmpty()) {
            Event event = eventProducer.getFailedEvents().poll(); // 从队列中取出一条消息进行重发
            eventProducer.fireEvnet(event);
        }
    }
}
