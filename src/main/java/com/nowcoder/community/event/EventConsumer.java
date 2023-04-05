package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import com.nowcoder.community.websocket.WebsocketService;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 开发事件的消费者
 */
@Component
public class EventConsumer implements CommunityConstant {
    //需要记录日志
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Autowired
    private RedisTemplate redisTemplate;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW}) //消费多个主题
    public void handleCommentMessage(ConsumerRecord record){ //ConsumerRecord用来接收数据
        if (record == null || record.value() == null){
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class); //将JSON字符串转回Event对象
        if (event == null){
            logger.error("消息格式错误！");
            return;
        }

        String messageId = event.getMessageId(); // 假设消息的ID字段为messageId
        String messageIdSetKey = RedisKeyUtil.getMessageIdKey(event.getTopic());
        if (StringUtils.isBlank(messageId)) {
            logger.error("消息id为空！");
            return;
        }

        // 检查Redis中是否已经处理过该消息
        if (redisTemplate.opsForSet().isMember(messageIdSetKey, messageId)) {
            logger.info("消息已经处理过，messageId: {}", messageId);
            return;
        }

        // 发站内信
        // 创一个message对象插入表中(系统发通知，假设from_id固定为1； conversation_id存主题topic；content内容存json字符串包含拼出一句话的各种条件)
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID); //规定系统用户为1
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType()); //事件作用于的实体类型
        content.put("entityId", event.getEntityId());

        //把event对象的其他额外数据存入content
        if (!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message); //存入数据库

//        WebsocketService.WEBSOCKET_MAP.get()

        // 将messageId加入到Redis的Set中，以标记该消息已经被处理过
        redisTemplate.opsForSet().add(messageIdSetKey, messageId);
        logger.info("消息处理完成，messageId: {}", messageId);

        // key越来越大...怎么处理？
        redisTemplate.expire(messageIdSetKey, 2, TimeUnit.HOURS);
    }

    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class); //将JSON字符串转回Event对象
        if (event == null){
            logger.error("消息格式错误！");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);

    }

    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class); //将JSON字符串转回Event对象
        if (event == null){
            logger.error("消息格式错误！");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    //消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功: " + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败: " + e.getMessage());
        }

    }

}
