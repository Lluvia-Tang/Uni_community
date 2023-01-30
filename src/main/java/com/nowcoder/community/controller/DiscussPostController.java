package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvnet(event);

        // 报错的情况，将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //查询帖子作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 查询点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        // 点赞状态
        // 未登录时点赞状态0
        int likeStatus = hostHolder.getUser() == null? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //查询评论信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论：给帖子的评论
        // 回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null){
            for (Comment comment : commentList){
                //一个评论的VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 评论的作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                // 查询点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // 点赞状态
                // 未登录时点赞状态0
                likeStatus = hostHolder.getUser() == null? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);//不再做分页

                //回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList!=null){
                    for (Comment reply:replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        //存入回复
                        replyVo.put("reply", reply);
                        //回复作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复目标target
                        User target = reply.getTargetId() == 0? null: userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        // 查询点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        // 点赞状态
                        // 未登录时点赞状态0
                        likeStatus = hostHolder.getUser() == null? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "site/discuss-detail";
    }

    //置顶请求、取消置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody //异步请求
    public String setTop(int id){
        DiscussPost post = discussPostService.findDiscussPostById(id);
        // 获取新的置顶状态 ，1为置顶，0为正常状态,1^1=0 0^1=1 使用异或实现
        int new_type = post.getType() ^ 1;
        discussPostService.updateType(id, new_type);
//        discussPostService.updateType(id, 1);

        //返回的结果,当前的置顶状态
        Map<String, Object> map = new HashMap<>();
        map.put("type", new_type);

        //触发发帖事件,把最新的帖子数据同步到elasticsearch中以便搜索到
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvnet(event);

        return CommunityUtil.getJSONString(0, null, map);
    }

    //加精请求、取消加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody //异步请求
    public String setWonderful(int id){
        DiscussPost post = discussPostService.findDiscussPostById(id);
        // 1为加精，0为正常， 1^1=0, 0^1=1
        int new_status = post.getStatus() ^ 1;
        discussPostService.updateStatus(id, new_status);
//        discussPostService.updateStatus(id, 1);

        //返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("status", new_status);

        //触发发帖事件,把最新的帖子数据同步到elasticsearch中以便搜索到
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvnet(event);

        return CommunityUtil.getJSONString(0, null, map);
    }

    //删除请求
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody //异步请求
    public String setDelete(int id){
        discussPostService.updateStatus(id, 2);

        //触发发帖事件,把最新的帖子数据同步到elasticsearch中以便搜索到
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvnet(event);

        return CommunityUtil.getJSONString(0);
    }

}
