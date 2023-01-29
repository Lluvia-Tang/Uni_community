package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null){
            user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 注册用户 ==> 把用户信息存入数据库
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0); //普通用户
        user.setStatus(0); //用户状态为未注册
        user.setActivationCode(CommunityUtil.generateUUID()); //设置激活码
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000))); //设置随机头像
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发激活邮件（html邮件带链接）
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context); //依据模板生成邮件内容
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        //验证账号合法性
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg", "该账号不存在");
            return map;
        }

        //验证状态
        if (user.getStatus() == 0) { //没有激活
            map.put("usernameMsg", "该账号未激活");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (user.getPassword().equals(password)){
            map.put("passwordMsg", "密码不正确");
            return map;
        }

        //登陆成功，生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID()); //ticket为随机生成字符串
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        // 将凭证存入Redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket()); //传递登录凭证
        return map;
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    //获取邮箱验证码
    public Map<String, Object> getCode(String email){
        Map<String, Object> map = new HashMap<>();
        //空值判断
        if (StringUtils.isBlank(email)){
            map.put("emailMsg","请输入邮箱！");
            return map;
        }
        //验证邮箱是否正确
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("emailMsg","该邮箱还未注册过，请注册后再使用！");
            return map;
        }

        //验证该邮箱是否已激活
        if (user.getStatus() == 0){
            map.put("emailMsg","该邮箱还未激活，请到邮箱中激活后再使用！");
            return map;
        }

        //邮箱正确的情况下，发送验证码到邮箱
        Context context = new Context();
        context.setVariable("email",email);
        String code = CommunityUtil.generateUUID().substring(0,5);
        context.setVariable("verifyCode",code);
        String content = templateEngine.process("mail/forget", context);
        mailClient.sendMail(email, "牛客网验证码", content);

        map.put("code",code);//map中存放一份，为了之后和用户输入的验证码进行对比
        map.put("expirationTime", LocalDateTime.now().plusMinutes(5L));//过期时间
        return map;
    }

    //忘记密码
    public Map<String, Object> forget(String email, String verifycode, String password){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(email)){
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        if (StringUtils.isBlank(verifycode)){
            map.put("codeMsg","请输入验证码！");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        User user = userMapper.selectByEmail(email);

        //加密处理密码
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);

        return map;
    }

    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg", "原密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }

        // 密码长度小于6位
        if (oldPassword.length() < 6){
            map.put("oldPasswordMsg", "密码长度不能小于8位!");
            return map;
        }
        if (newPassword.length() < 6){
            map.put("newPasswordMsg", "密码长度不能小于8位!");
        }

        //验证原密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)){
            map.put("oldPasswordMsg", "原密码输入有误！");
            return map;
        }

        //更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        return map;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.当数据变更时，清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    // 查询用户的权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
