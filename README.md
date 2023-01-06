# community

#### 介绍
newcoder community项目

#### 开发功能
- 社区首页功能 --1.4
- 发送邮件(Spring Email)  --1.5
- 注册功能  --1.5
- 生成验证码  --1.6
- 开发登录、退出功能  --1.6
- 忘记密码  --1.6


##### 开发社区首页
- 开发流程
    + 1次请求的执行过程（--> controller --> service --> dao --> datasets）
- 分步实现
    + 开发社区首页，显示前十个帖子
    + 开发分页组件，分页显示所有的帖子

##### 发送邮件（为注册做准备）
- 邮箱设置
    + 启用客户端SMTP服务
- Spring Email
    + 导入jar包/pom.xml中坐标
    + 邮箱参数配置
    + 使用JavaMailSender发送邮件
- 模板引擎
    + 使用Thymeleaf发送HTML邮件

##### 开发注册功能
- 访问注册页面
  - 点击顶部区域内的连接，打开注册页面
- 提交注册数据（三层架构）
  - 通过表单提交数据
  - 服务端验证账号是否已存在、邮箱是否已注册
  - 服务端发送激活邮件
- 激活注册账号
  - 点击邮件中的链接，访问服务端的激活服务

##### 生成验证码
- Kaptcha
  - 导入jar包
  - 编写Kaptcha配置类
  - 生成随机字符、生成图片
  - 服务端将验证码存入Session（隐私数据），将图片输出给浏览器
- 显示到login页面
- 每次点击刷新验证码时会更新图片

##### 开发登录、退出功能
- 访问登录页面
  - 点击顶部区域内的连接，打开登录页面
- 登录
  - 验证账号、密码、验证码
  - 成功时，生成登录凭证（有敏感数据，存到数据库里方便以后Redis），发放给客户端（Cookie存储凭证）
  - 失败时，跳转回登录页
- 退出
  - 将登录凭证修改为失效状态
  - 跳转至网站首页

##### 忘记密码
- 访问忘记密码页面
  - 点击登录页面的忘记密码按钮，跳转至忘记密码页面
- 向邮箱发送验证码
  - 输入邮箱之后，验证邮箱有效性后，点击获取验证码
  - 使用JavaMailSender为输入邮箱发送验证码
  - 将验证码和过期时间保存到session中以备后续验证
- 重置密码
  - 验证验证码有效
  - 调用service层更新数据库中用户的密码



#### 问题
- @SpringBootTest 单元测试JUnit不同版本的使用问题

  运行测试类时出现：Disconnected from the target VM, address: '127.0.0.1:56597', transport: 'socket'报错
  原因：不同测试版本的配置混用导致端口冲突
  - Junit版本问题
  - JUnit4引入的注解是 org.junit.Test 作为@Test注解。保证springboot测试类正常运行要加上两个注解 @SpringBootTest ， @RunWith(SpringRunner.class)
  - 但spring-boot-starter-test在2.2以上不再需要两个配置，JUnit5引入的注解是 org.junit.jupiter.api.Test 作为@Test注解。springboot测试类正常运行只需要加上 @SpringBootTest 注解即可，不需要加 @RunWith(SpringRunner.class)

-  session 最好是在表现层使用（controller）
