# community

#### 介绍
高校校园集市信息分享平台

#### 开发功能
- 集市社区首页功能 --1.4
- 发送邮件(Spring Email)  --1.5
- 注册功能  --1.5
- 生成验证码(Kaptcha)  --1.6
- 开发登录、退出功能  --1.6
- 忘记密码  --1.6
- 显示登录信息(Interceptor)  --1.9
- 账号设置(上传头像，更改密码)  --1.9
- 检查登录状态  --1.9
- 过滤敏感词(前缀树)  --1.10
- 发布帖子  --1.10
- 帖子详情  --1.13
- 显示评论(Transaction)  --1.13
- 添加评论  --1.14
- 私信列表  --1.15
- 发送私信  --1.15
- 删除私信  --1.15
- 在表现层进行统一异常处理/(引入AOP)在访问业务前统一记录访问日志  --1.16
- 点赞(Redis)  --1.17
- 个人主页->我收到的赞  --1.18
- 关注、取消关注  --1.18
- 关注列表、粉丝列表  --1.18
- 个人主页->我的帖子 --1.19
- 个人主页->我的回复  --1.19
- 优化登录模块  --1.20
- 发送系统通知(Kafka)  --1.21
- 显示系统通知  --1.21
- 开发校园集市搜索功能(Elasticsearch)  --1.27
- 权限控制  --1.29
- 置顶、加精、删除/取消置顶、取消加精(Security)  --1.30
- 网站数据统计(HyperLogLog,Bitmap)  --1.31
- 热帖排行(Quartz)  --2.1
- 生成长图  --2.1
- 优化网站的性能(Caffeine)  --2.2
- 项目发布-单元测试  --2.2
- 项目发布-项目监控  --2.3

##### 开发校园集市社区首页
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

##### 显示登录信息
- 拦截器示例
  - 定义拦截器，实现HandlerInterceptor
  - 配置拦截器，为它指定拦截、排除的路径
- 拦截器应用
  - 在请求开始时查询登录用户(从Cookie中查询ticket)
  - 在本次请求中持有用户数据(使用ThreadLocal进行线程隔离的持有user)
  - 在模板视图上显示用户数据
  - 在请求结束时清理用户数据

##### 账号设置（上传头像）
- 上传文件
  - 请求：必须是POST请求
  - 表单： enctype="multipart/form-data"
  - Spring MVC: 通过MultipartFile处理上传文件
- 开发步骤
  - 访问账号设置页面
  - 上传头像 （之后获取user对象都从hostHolder中获得请求中持有的用户数据）
  - 获取头像

##### 检查登录状态
现在即使未登录，也可以根据路径进入某些功能页面，要加以处理
- 使用拦截器
  - 在方法前标注自定义注解
  - 拦截所有请求，只处理带有该注解的方法
- 自定义注解
  - 常用的元注解
    - @Target、@Retention、@Document、@Inherited
  - 如何读取注解
    - Method.getDeclaredAnnotations()
    - Method.getAnnotation(Class<T> annotationClass)

##### 过滤敏感词
- 前缀树
  - 名称：Trie、字典树、查找树
  - 特点：查找效率高，消耗内存大
  - 应用：字符串检索、词频统计、字符串排序等
- 敏感词过滤器
  - 定义前缀树
  - 根据敏感词，初始化前缀树
  - 编写过滤敏感词的方法

##### 发布帖子
- AJAX
  - 异步JavaScript与XML
  - 使用AJAX，网页能够将增量更新呈现在页面上，而不需要刷新整个页面
  - 服务器返回JSON字符串
- 示例
  - 使用jQuery发送AJAX请求
- 实践
  - 采用AJAX请求，实现发布帖子的功能
  - 使用编写的工具类得到JSON串
  - Controller中不返回页面时要在方法前加上@ResponseBody注解

##### 帖子详情
- DiscussPostMapper
- DiscussPostService
- DiscussPostController
- index.html
  - 在帖子标题上增加访问详情页面的链接
- discuss-detail.html
  - 处理静态资源的访问路径
  - 复用index.html的header区域
  - 显示标题、作者、发布时间、帖子正文等内容

##### 显示评论
- 数据层
  - 根据实体查询一页评论数据
  - 根据实体查询评论的数量
- 业务层
  - 处理查询评论的业务
  - 处理查询评论数量的业务
- 表现层
  - 显示帖子详情数据时，同时显示该帖子所有的评论数据

##### 添加评论
- 数据层
  - 增加评论数据
  - 修改帖子的评论数量
- 业务层
  - 处理添加评论的业务
  - 先增加评论、再更新帖子的评论数量
- 表现层
  - 处理添加评论数据的请求
  - 设置添加评论的表单

##### 私信列表
- 私信列表
  - 查询当前用户的会话列表，每个会话只显示一条最新的私信（多次私信为一个会话）
  - 支持分页显示
- 私信详情
  - 查询每个会话所包含的私信
  - 支持分页显示

##### 发送私信
- 发送私信
  - 采用异步的方式发送私信
  - 发送成功后刷新私信列表
- 设置已读
  - 访问私信详情时，将显示的私信设置为已读状态

##### 删除私信
- 点击删除按钮时，向服务端发送id
- 在service中将该条私信状态更新为2（删除态）

##### 点赞
- 点赞（将数据存入redis提升效率）
  - 支持对帖子、评论点赞
  - 第一次点赞，第二次取消点赞
  - Redis, 使用Redis可以不用写数据访问层，直接写业务层
  - 为RedisKey的生成写一个工具，方便复用
- 首页点赞数量(表现层异步请求，不刷新页面即显示)
  - 统计帖子的点赞数量
- 帖子详情页点赞数量
  - 统计点赞数量
  - 显示点赞状态

##### 我收到的赞
- 重构点赞功能
  - 以用户为RedisKey，记录点赞数量
  - increment(key)，decrement(key)
- 开发个人主页
  - 以用户为key，查询点赞数量

##### 关注、取消关注
- 需求
  - 开发关注。取消关注功能
  - 统计用户的关注数、粉丝数
- 关键
  - 若A关注了B，则A是B的Follower，B是A的Followee
  - 关注的目标可以是用户、帖子、题目等，在实现时将这些目标抽象为实体
  - Redis存储
    - 存储某个用户关注的目标实体 followee: userId: entityType -> zset(entityId, nowTime)
    - 存储某个实体拥有的粉丝 follower: entityType: entityId -> zset(userId, nowTime)
  - 同时操作两个RedisKey的新增需要添加事务
  - 需要查询实体的粉丝数，和用户的关注目标实体的数量
  - 表现层的关注和取消关注是异步请求

##### 关注列表、粉丝列表
- 业务层
  - 查询某个用户关注的人，支持分页(List<Map<String, Object>>)
  - 查询某个用户的粉丝，支持分页
- 表现层
  - 处理“查询关注的人”、“查询粉丝”请求
  - 编写“查询关注的人”、“查询粉丝”模板

##### 我的帖子、我的回复
- 业务层
  - 查询某个用户发表的帖子，支持分页
  - 查询某个用户发表过的帖子回复，支持分页
- 表现层
  - 处理和编写“查询发表过的帖子”、“查询发表评论”请求和模板
  - 页面最上方显示发表的总条数

##### Redis优化登录模块(提升性能)
- 使用Redis存储验证码
  - 验证码需要频繁的访问与刷新，对性能要求较高
  - 验证码不需永久保存，通常在很短的时间后就会失效
  - 分布式部署时，存在Session共享的问题
- 使用Redis存储登录凭证（不使用mysql中的login_ticket表）
  - 处理每次请求时，都要查询用户的登录凭证，访问的频率非常高
- 使用Redis缓存用户信息
  - 处理每次请求时，都要根据凭证查询用户信息，访问的频率非常高
- 具体做法
  - 在RedisKeyUtil中定义对应的key

##### 发送系统通知
- 触发事件
  - 评论后，发布通知
  - 点赞后，发布通知
  - 关注后，发布通知
  - 多个topic主题
- 处理事件
  - 封装事件对象（新的实体类）
  - 开发事件的生产者
  - 开发事件的消费者
  - 修改controller中事件的触发
    - 注入EventProducer
    - 新建Event对象，设置对应的属性数据
    - 发布消息：eventProducer.fireEvnet(event);

##### 显示系统通知
- 通知列表
  - 显示评论、点赞、关注三种类型的通知
- 通知详情
  - 分页显示某一类主题所包含的通知
- 未读消息
  - 在页面头部显示所有的未读消息数量(用拦截器处理（每个请求都有）)

##### 开发社区搜索功能
- 搜索服务（elasticsearchService）
  - 将帖子保存至Elasticsearch服务器
  - 从Elasticsearch服务器删除帖子
  - 从Elasticsearch服务器搜索帖子
- 发布事件
  - 发布帖子时，将帖子异步的提交到Elasticsearch服务器(在发帖业务中触发发帖事件)
  - 增加评论时，将帖子异步的提交到Elasticsearch服务器
  - 在消费组件中增加一个方法，消费帖子发布事件
- 显示结果
  - 在控制器中处理搜索请求，在HTML上显示搜索结果

##### 权限控制
- 登录检查
  - 之前采用拦截器实现了登录检查，这是简单的权限管理方案，现在将其废弃
- 授权配置
  - 对当前系统内包含的所有请求，分配访问权限（普通用户、版主、管理员）
- 认证方案
  - 绕过Security认证流程，采用系统原来的认证方案
- CSRF配置
  - 防止CSRF攻击的基本原理，以及表单、AJAX相关的配置

##### 置顶、加精、删除/取消置顶、取消加精
- 功能实现
  - 点击 置顶，修改帖子的类型
  - 点击“加精”、“删除”，修改帖子的状态
  - 使用kafka消息队列完成，不是直接在controller中调es的service中的业务功能
    - 由于各个中间件部署在不同的机器上，es服务器和应用服务器需要通信，网络通信耗时，应用服务器等待的响应慢，所以把这些操作交给kafka异步处理
- 极限管理
  - 版主可以执行“置顶”、“加精”操作
  - 管理员可以执行“删除”操作
- 按钮显示(Security权限控制)
  - 版主可以看到“置顶”、“加精”按钮
  - 管理员可以看到“删除”按钮

##### 网站数据统计
- UV（Unique Visitor）
  - 独立访客，需通过用户IP去重统计数据
  - 每次访问都要进行统计(在拦截器内记录ip至redis)
  - 使用redis高级数据类型：HyperLogLog，性能好，且存储空间小（近似的结果）
  - 在controller显示层中统计输入日期间的uv（dataService.calculateUV(start, end)
- DAU（Daily Active User）
  - 日活跃用户，需通过用户ID去重统计数据
  - 访问过一次，则认为其活跃(在拦截器内记录userid至redis)
  - 使用redis高级数据类型：Bitmap，性能好，且可以统计精确的结果
  - 在controller显示层中统计输入日期间的dau（dataService.calculateDAU(start, end)
- 只有管理员可以访问/data路径下的统计功能

##### 热帖排行
- Hacker News
  - Score = (P-1) / (T+2) ^ G
  - P:投票数，T:发布到现在的时间，G:系数1.5~1.8
  - 分数与受欢迎的人数（喜好程度）成正比，与时间成反比
- StackOverflow
  - (log(Qviews)*4) + ((Qanswers * Qscore)/5) + sum(Ascores)
  - Qviews:问题的浏览数量，Qanswers:问题的回答数量，Qscore:点赞数-点踩数，Ascores:回答的赞踩差
  - ----------------------------------------------------------
  - ((QageInHours + 1) - ((QageInHours - Qupdated)/2)) ^ 1.5
  - QageInHours:题目发布的时间，Qupdated:更新的时间
- Nowcoder
  - log(精华分 + 评论数*10 + 点赞数*2 + 收藏数*2) + (发布事件 - 牛客纪元)
- 设置定时任务（Quartz），每5分钟计算分数，实现帖子按分数排序
  - 不是计算所有的帖子，把分数变化的帖子放入缓存中（redis），时间到了把缓存中的帖子计算
  - 新增discusspost、点赞、评论时更新redis
  - 在Quartz中实现更新（同时数据需要更新到搜索数据中elasticsearch）

##### 生成长图
- wkhtmltopdf
  - 命令行
  - wkhtmltopdf url file
  - wkhtmltoimage url file
- java
  - Runtime.getRuntime().exec()
- 使用程序开发分享功能（生成长图）
  - 在application.yml中配置wk的storage和command位置
  - 在WkConfig中配置服务启动时创建WK图片目录storage
  - 在ShareController中利用kafka队列异步实现生成长图功能（定义Event）和获取长图功能
  - 在EventConsumer中消费分享事件：拼出cmd ==> Runtime.getRuntime().exec(cmd);

##### 将文件上传至云服务器
- 重构、开发功能
  - 重构头像功能，在客户端直接将头像传至七牛云
  - 重构分享功能，在服务器将生成的图片上传至七牛云
  - 启动定时任务（每4分钟执行一次），删除1分钟之前由分享功能所创建的临时文件
- 客户端上传
  - 客户端将数据提交给云服务器，并等待其相应
  - 用户上传头像时，将表单数据提交给云服务器
- 服务器直传
- 应用服务器将数据直接提交给云服务器，并等待其相应
- 分享时，服务端将自动生成的图片，直接提交给云服务器

##### 优化网站的性能
- 本地缓存
  - 将数据缓存在应用服务器上，性能最好，访问速度快
  - 常用缓存工具：Ehcache、Guava、Caffeine(性能最好)等
  - 适合更新没有那么频繁，实时性要求较低的数据，可以提升访问速度
- 分布式缓存
  - 将数据缓存在NoSQL数据库上，跨服务器
  - 常用缓存工具：MemCache、Redis等
  - 适合更新频率比较快的功能
- 构造多级缓存
  - -> 一级缓存（本地缓存） > 二级缓存（分布式缓存） > DB
  - 避免缓存雪崩（缓存失效，大量请求直达DB），提高系统的可用性
- 使用Jmeter做压力测试，观察增加缓存前、后的吞吐量
- 什么样的数据适合本地缓存？
  - 数据变化的频率相对较低，不用过于频繁的更新缓存
  - 使用Caffeine，缓存热门的帖子列表(优化业务方法service)

##### 项目发布前需要的工作
1. 单元测试
   - Spring Boot Testing
     - 依赖：spring-boot-starter-test
     - 包括：junit、spring test、AssertJ、...
   - Test Case 测试用例
     - 要求：保证测试方法的独立性(随时随地都能执行)
     - 步骤：初始化数据、执行测试代码、验证测试结果、清理测试数据
     - 常用注解：@BeforeClass、@AfterClass、@Before、@After
2. 项目监控
   - Spring Boot Actuator
     - Endpoints: 监控应用的入口，Spring Boot内置了很多端点，也支持自定义端点(自定义一个DatabaseEndpoint.java)
     - 监控方式: HTTP或JMX
     - 访问路径: 例如 "/actuator/health" 返回当前服务的健康状态； "/actuator/info" 返回服务端的信息； “/actuator/beans”返回所有的bean
     - 注意事项: （application.yml中）按需配置暴露的端点，并对所有端点进行权限控制(在SecurityConfig里面添加"/actuator/**"权限仅给管理员)



#### 问题
- @SpringBootTest 单元测试JUnit不同版本的使用问题

  运行测试类时出现：Disconnected from the target VM, address: '127.0.0.1:56597', transport: 'socket'报错
  原因：不同测试版本的配置混用导致端口冲突
  - Junit版本问题
  - JUnit4引入的注解是 org.junit.Test 作为@Test注解。保证springboot测试类正常运行要加上两个注解 @SpringBootTest ， @RunWith(SpringRunner.class)
  - 但spring-boot-starter-test在2.2以上不再需要两个配置，JUnit5引入的注解是 org.junit.jupiter.api.Test 作为@Test注解。springboot测试类正常运行只需要加上 @SpringBootTest 注解即可，不需要加 @RunWith(SpringRunner.class)

-  session 最好是在表现层使用（controller）

- 解决 Failed to obtain JDBC Connection； nested exception is java.sql.SQLNonTransientConnectionException，重新配置项目时忽然jdbc报错
   - 在yml配置文件中数据源的url配置地址后面加 “&allowPublicKeyRetrieval=true”
