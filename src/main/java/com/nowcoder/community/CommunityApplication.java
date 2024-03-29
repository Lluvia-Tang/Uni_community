package com.nowcoder.community;

import com.nowcoder.community.websocket.WebsocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	@PostConstruct
	public void init(){
		// 解决netty启动冲突问题(elasticsearch和redis的底层都依赖于netty)
		// see Netty4Utils.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors","false");

	}

	public static void main(String[] args) {
//		ApplicationContext app = SpringApplication.run(CommunityApplication.class, args);
		SpringApplication.run(CommunityApplication.class, args);
//		WebsocketService.setApplicationContext(app);
	}
}
