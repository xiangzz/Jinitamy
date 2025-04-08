package com.jinitamy.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Web引擎核心类
 * 
 * 该类是框架的核心组件，负责：
 * 1. 管理HTTP服务器
 * 2. 处理路由注册
 * 3. 管理中间件
 * 4. 配置服务器参数
 * 
 * 使用示例：
 * <pre>
 * Engine engine = new Engine();
 * engine.setPort(8080);
 * engine.get("/", ctx -> {
 *     ctx.getResponse().content().writeBytes("Hello World".getBytes());
 * });
 * engine.start();
 * </pre>
 */
public class Engine {
    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);
    
    /** 路由管理器 */
    private final Router router;
    /** 中间件列表 */
    private final List<Middleware> middlewares;
    /** 服务器端口号，默认为8080 */
    private int port = 8080;

    /**
     * 构造函数
     * 初始化路由管理器和中间件列表
     */
    public Engine() {
        this.router = new Router();
        this.middlewares = new ArrayList<>();
    }

    /**
     * 设置服务器端口号
     * 
     * @param port 要设置的端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取当前服务器端口号
     * 
     * @return 当前端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 添加中间件
     * 
     * 中间件按照添加顺序依次执行，可以用于：
     * 1. 请求预处理
     * 2. 日志记录
     * 3. 权限验证
     * 4. 响应后处理
     * 
     * @param middleware 要添加的中间件
     */
    public void use(Middleware middleware) {
        middlewares.add(middleware);
    }

    /**
     * 注册GET请求路由
     * 
     * @param pattern URL匹配模式，支持动态参数（如：/user/:id）
     * @param handler 请求处理器
     */
    public void get(String pattern, Handler handler) {
        router.addRoute(HttpMethod.GET, pattern, handler);
    }

    /**
     * 注册POST请求路由
     * 
     * @param pattern URL匹配模式，支持动态参数（如：/user/:id）
     * @param handler 请求处理器
     */
    public void post(String pattern, Handler handler) {
        router.addRoute(HttpMethod.POST, pattern, handler);
    }

    /**
     * 启动HTTP服务器
     * 
     * 该方法会：
     * 1. 创建事件循环组
     * 2. 配置服务器引导程序
     * 3. 设置通道处理器
     * 4. 绑定端口并启动服务器
     * 
     * @throws Exception 当服务器启动失败时抛出
     */
    public void start() throws Exception {
        // 创建主事件循环组（用于接收连接）
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 创建工作事件循环组（用于处理连接）
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())  // HTTP编解码器
                                    .addLast(new HttpObjectAggregator(65536))  // HTTP消息聚合器
                                    .addLast(new HttpHandler(Engine.this));  // 自定义HTTP处理器
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)  // 设置连接队列大小
                    .childOption(ChannelOption.SO_KEEPALIVE, true);  // 启用TCP keepalive

            // 绑定端口并启动服务器
            b.bind(port).sync();
            logger.info("Server started on port {}", port);
        } catch (Exception e) {
            logger.error("Server start failed", e);
            // 优雅关闭事件循环组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            throw e;
        }
    }

    /**
     * 获取路由管理器
     * 
     * @return 路由管理器实例
     */
    public Router getRouter() {
        return router;
    }

    /**
     * 获取中间件列表
     * 
     * @return 中间件列表
     */
    public List<Middleware> getMiddlewares() {
        return middlewares;
    }
} 