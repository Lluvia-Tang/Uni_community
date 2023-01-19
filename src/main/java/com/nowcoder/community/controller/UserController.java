package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import sun.security.util.Password;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder; //取对象

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    //上传图片
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确");
            return "/site/setting";
        }

        //生成随机文件名
       fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        //更新当前用户的头像路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //读取图片
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        // 服务器端存放路径
        filename = uploadPath + "/" + filename;
        // 文件后缀
        String suffix = filename.substring(filename.lastIndexOf('.'));
        // 相应图片
        response.setContentType("image/" +suffix);
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(filename);
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" +e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model){
        User user = hostHolder.getUser();
        //两次密码不一致放在前端检验register.js
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()){
            return "redirect:/logout";
        }else{
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }

        //用户
        model.addAttribute("user", user);
        //用户获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){ //当前是否登录
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    // 我的帖子
    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        //设置分页
        page.setLimit(10);
        page.setPath("/user/mypost/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));

        //帖子列表
        List<DiscussPost> discussList = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        // 设置VOList分别存储每个帖子的帖子信息和帖子被点赞数量信息
        List<Map<String, Object>> discussVOList = new ArrayList<>();
        if (discussList != null){
            for (DiscussPost post : discussList){
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost", post);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussVOList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussVOList);

        return "/site/my-post";
    }

    //我的回复
    @RequestMapping(path = "/myreply/{userId}", method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        //设置分页
        page.setLimit(10);
        page.setPath("/user/myreply/" + userId);
        page.setRows(commentService.findUserCount(userId));

        //回复列表
        List<Comment> commentList = commentService.findUserComments(userId, page.getOffset(), page.getLimit());
        // 封装到列表中，封装评论的帖子的信息和评论信息
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null){
            for (Comment comment: commentList){
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                // 通过评论中的评论实体id找到对应的帖子
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("discussPost", post);
                commentVOList.add(map);
            }
        }
        model.addAttribute("comments", commentVOList);

        return "/site/my-reply";
    }

}
