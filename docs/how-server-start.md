### 初始化阶段

1. **Engine类**：框架的核心入口点
   - 创建Engine实例 `Engine engine = new Engine()`
   - 内部初始化Router和中间件列表
   - 提供配置方法如`setPort()`

2. **中间件注册**：
   - 通过`engine.use(Middleware)`添加中间件
   - 中间件按添加顺序存储在列表中
   - 中间件实现`Middleware`接口，处理请求并决定是否继续处理链

3. **路由注册**：
   - 通过`engine.get(pattern, handler)`等方法注册路由
   - 内部调用`router.addRoute(HttpMethod, pattern, handler)`
   - Router将路由规则解析为路径片段，构建前缀树结构
   - 处理器实现`Handler`接口，负责具体业务逻辑

### 服务器启动阶段

1. **Engine.start()**：
   - 创建Netty的事件循环组（bossGroup和workerGroup）
   - 配置ServerBootstrap，设置通道选项
   - 添加通道处理器，包括HTTP编解码器和自定义HttpHandler
   - 绑定端口并启动服务器

### 请求处理阶段

1. **HttpHandler.channelRead0()**：接收HTTP请求
   - 创建响应对象和Context上下文
   - 调用`engine.getRouter().getRoute()`查找匹配的路由处理器
   - 构建中间件链（从后往前包装）
   - 执行处理链

2. **Router.getRoute()**：路由匹配
   - 解析请求路径为片段数组
   - 在前缀树中查找匹配的节点
   - 如果是动态参数，将参数值保存到Context中
   - 返回匹配的Handler或null

3. **中间件链执行**：
   - 从第一个中间件开始，依次调用`middleware.handle(context, next)`
   - 每个中间件可以在调用next前后执行逻辑
   - 最后一个next是路由处理器

4. **Handler.handle()**：业务逻辑处理
   - 处理具体的业务逻辑
   - 通过Context访问请求信息和设置响应

5. **响应发送**：
   - 调用`writeResponse()`发送HTTP响应
   - 如果发生错误，调用`sendError()`发送错误响应

### 核心组件关系

1. **Engine**：
   - 包含Router实例，负责路由管理
   - 维护中间件列表
   - 提供HTTP方法的路由注册接口
   - 负责服务器的启动和配置

2. **Router**：
   - 使用前缀树结构存储路由规则
   - 支持静态路由、动态参数和通配符
   - 提供路由匹配功能

3. **Context**：
   - 封装HTTP请求和响应
   - 存储路由参数和自定义属性
   - 提供访问请求信息和设置响应的接口

4. **HttpHandler**：
   - Netty的ChannelHandler实现
   - 负责请求接收、路由匹配和中间件链执行
   - 处理异常并发送响应

5. **Handler接口**：
   - 定义请求处理的标准方法
   - 实现具体的业务逻辑

6. **Middleware接口**：
   - 定义中间件的标准方法
   - 实现请求的预处理和后处理逻辑

这种设计使Jinitamy框架具有良好的模块化和可扩展性，遵循了责任分离原则，每个组件负责特定的功能，共同协作完成HTTP请求的处理流程。
        