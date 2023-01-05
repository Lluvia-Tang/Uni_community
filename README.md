# community

#### 介绍
newcoder community项目

#### 开发功能
- 社区首页功能 --1.4
- 发送邮件(Spring Email)


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



