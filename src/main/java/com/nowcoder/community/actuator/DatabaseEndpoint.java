package com.nowcoder.community.actuator;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@Component
@Endpoint(id = "database") //自定义端点的路径 //还需要权限管理
public class DatabaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    //尝试获取连接
    @Autowired
    private DataSource dataSource; //连接池

    @ReadOperation //@WriteOperation:post
    public String checkConnection(){
        try (Connection conn = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0, "获取连接成功！");
        }catch (SQLException e){
            logger.error("获取连接失败：" + e.getMessage());
            return CommunityUtil.getJSONString(1, "获取连接失败！");
        }
    }

}
