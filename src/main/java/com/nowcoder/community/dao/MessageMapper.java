package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Mapper
public interface MessageMapper {

    /**
     * 查询当前用户的会话列表，每个会话包含最新的一条私信
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * 查询当前用户的会话数量
     * @param userId
     * @return
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话所包含的私信列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId
     * @return
     */
    int selectLetterCount(String conversationId);

    /**
     * 查询未读私信的数量
     * @return
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     * 新增一个消息
     * @param message
     * @return
     */
    int insertMessage(Message message);

    /**
     * 修改消息的状态
     * @param ids
     * @param status
     * @return
     */
    int updateStatus(List<Integer> ids, int status);

    /**
     * 查询某一主题下的最新的一条系统通知
     */
    Message selectLatestNotice(int userId, String topic);

    /**
     * 查询某个主题所包含的系统通知的数量
     */
    int selectNoticeCount(int userId, String topic);

    /**
     * 查询未读的系统通知数量
     */
    int selectNoticeUnreadCount(int userId, String topic);

    /**
     * 查询某个主题所包含的通知列表
     */
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
