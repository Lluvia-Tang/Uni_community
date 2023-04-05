package com.nowcoder.community.websocket;
/**
 * websocket相关服务
 */
import com.nowcoder.community.controller.LoginController;
import com.nowcoder.community.util.HostHolder;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//@Component
//@ServerEndpoint("/comserver") //访问路径
public class WebsocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketService.class);

    // 长连接的数量 (在线人数统计)
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);
    // 高效获取客户端的websocket服务
    public static final ConcurrentHashMap<String, WebsocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();
    // 服务端和客户端的通信会话（和客户端关联）
    private Session session;
    // 存储客户端标识
    private String sessionId;

    // 全局上下文设置，多例模式下所有websocketService共用的全局变量
    // 通过APPLICATION_CONTEXT来获取想要的bean
    private static ApplicationContext APPLICATION_CONTEXT;
    /**
     * WebSocketService是多例模式，而SpringBoot是单例模式，导致想要的类没有被成功重复注入
     */
//    @Autowired
//    private RedisTemplate redisTemplate;
    public static void setApplicationContext(ApplicationContext applicationContext){
        WebsocketService.APPLICATION_CONTEXT = applicationContext;
    }

    /**
     * 连接成功时调用方法
     * @param session
     */
    @OnOpen
    public void openConnection(Session session){
        // 多例模式下直接使用@Autowired会有注入bean的问题
//        RedisTemplate redisTemplate = (RedisTemplate) WebsocketService.APPLICATION_CONTEXT.getBean("redisTemplate");
//        redisTemplate.opsForValue().get("dsds");

        this.sessionId = session.getId();
        this.session = session;
        if (WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        }else{
            WEBSOCKET_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("用户连接成功: " + sessionId + "，当前在线人数为：" + ONLINE_COUNT.get());
        try{
            this.sendMessage("0");
        }catch (Exception e){
            logger.error("连接异常");
        }
    }

    /**
     * 客户端断开连接
     */
    @OnClose
    public void closeConnection(){
        if (WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info("用户退出：" + sessionId + "当前在线人数为：" + ONLINE_COUNT.get());
    }

    /**
     * 当有消息通信时
     * @param message
     */
    @OnMessage
    public void onMessage(String message){
        logger.info("用户信息: " + sessionId + "，报文：" + message);
        if (!StringUtil.isNullOrEmpty(message)){
            try {
                // 服务器主动推送消息
                for (Map.Entry<String, WebsocketService> entry : WEBSOCKET_MAP.entrySet()){
                    // 获取websocketService
                    WebsocketService websocketService = entry.getValue();

                    if (websocketService.session.isOpen()){
                        websocketService.sendMessage(message);
                    }
                }

            }catch (Exception e){
                logger.error("系统通知推送有问题");
                e.printStackTrace();
            }
        }
    }

    /**
     * 发生错误时
     * @param error
     */
    @OnError
    public void onError(Throwable error){

    }

    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
}
