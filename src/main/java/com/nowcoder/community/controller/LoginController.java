package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage(){
        return "/site/forget";
    }

    @RequestMapping(path = "/register", method =RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target", "/index");
            return "/site/operate-result"; //返回要去的路径
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable int userId, @PathVariable String code){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经激活过！");
            model.addAttribute("target", "/index");
        }else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response /*, HttpSession session*/){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入Session
//        session.setAttribute("kaptcha", text);

        // 将验证码存入Redis
        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath); //整个项目下有效
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("相应验证码失败：" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*, HttpSession session*/, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){
        //检查验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        // 判断验证码是否有效
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        //检查账号，密码
        int expiredSeconds = rememberme? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            //登陆成功,把登录凭证保存到客户端cookie中
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    //登出
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket); //状态设置为无效
        //清理权限
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    //获取验证码
    @RequestMapping(path = "/getCode", method = RequestMethod.GET)
    public String getCode(String email, Model model, HttpSession session){
        Map<String, Object> map = userService.getCode(email); //实现了验证邮箱有效性且发送验证码操作
        if (map.containsKey("emailMsg")) {//有错误的情况下
            model.addAttribute("emailMsg", map.get("emailMsg"));
        } else {//正确的情况下，向邮箱发送了验证码
            model.addAttribute("msg", "验证码已经发送到您的邮箱，5分钟内有效！"); //显示成功信息到客户端
            //将验证码存放在 session 中，后序和用户输入的信息进行比较
            session.setAttribute("code",map.get("code"));
            //后序判断用户输入验证码的时候验证码是否已经过期
            session.setAttribute("expirationTime",map.get("expirationTime"));
        }
        return "site/forget";
    }

    //忘记密码
    @RequestMapping(path = "/forget", method = RequestMethod.POST)
    public String forget(Model model, String email, String verifycode, String password, HttpSession session) {
        //验证验证码是否正确
        if (!verifycode.equals(session.getAttribute("code"))) {
            model.addAttribute("codeMsg", "输入的验证码不正确！");
            return "site/forget";
        }
        //验证码是否过期
        if (LocalDateTime.now().isAfter((LocalDateTime) session.getAttribute("expirationTime"))) {
            model.addAttribute("codeMsg", "输入的验证码已过期，请重新获取验证码！");
            return "site/forget";
        }

        Map<String, Object> map = userService.forget(email, verifycode, password);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "密码修改成功，可以使用新密码登录了!");
            model.addAttribute("target", "/login");
            return "site/operate-result";
        } else { //返回错误信息给客户端
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("codeMsg", map.get("codeMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }

}
