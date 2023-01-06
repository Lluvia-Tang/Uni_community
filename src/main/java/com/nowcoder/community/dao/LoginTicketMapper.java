package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket (user_id, ticket, status, expired) " +
                    "values(#{userId}, #{ticket}, #{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id") //赋予id字段自增长主键(配置项对注解形式无效)
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select * from login_ticket where ticket =#{ticket};"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
//            "<script>" +
            "update login_ticket set status = #{status} where ticket = #{ticket}" ,
//            "<if test = \"ticket!=null\">",
//            "and l=1",
//            "</if>"
//            "</script>"
    })
    int updateStatus(String ticket, int status);
}
