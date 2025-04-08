package com.jinitamy.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * HTTP请求处理器
 * 
 * 该类是Netty的ChannelHandler实现，负责：
 * 1. 接收HTTP请求
 * 2. 路由匹配
 * 3. 中间件链执行
 * 4. 响应发送
 * 5. 异常处理
 * 
 * 处理流程：
 * 1. 接收HTTP请求
 * 2. 创建请求上下文
 * 3. 查找匹配的路由处理器
 * 4. 构建中间件链
 * 5. 执行处理链
 * 6. 发送响应
 */
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);
    /** Web引擎实例 */
    private final Engine engine;

    /**
     * 创建HTTP处理器
     * 
     * @param engine Web引擎实例，用于路由匹配和中间件管理
     */
    public HttpHandler(Engine engine) {
        this.engine = engine;
    }

    /**
     * 处理接收到的HTTP请求
     * 
     * 该方法实现了完整的请求处理流程：
     * 1. 创建响应对象
     * 2. 创建请求上下文
     * 3. 查找路由处理器
     * 4. 构建中间件链
     * 5. 执行处理链
     * 6. 发送响应
     * 
     * @param ctx Netty通道上下文
     * @param request HTTP请求对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 创建响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.buffer()
        );

        // 创建上下文
        Context context = new Context(request, response);

        try {
            // 查找路由处理器
            Handler handler = engine.getRouter().getRoute(request.method(), request.uri(), context);
            
            if (handler == null) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }

            // 构建中间件链
            Handler finalHandler = (Context c) -> {
                handler.handle(c);
                writeResponse(ctx, c.getResponse());
            };

            // 从后往前包装中间件
            for (int i = engine.getMiddlewares().size() - 1; i >= 0; i--) {
                Middleware m = engine.getMiddlewares().get(i);
                Handler next = finalHandler;
                finalHandler = (Context c) -> m.handle(c, next);
            }

            // 执行处理链
            finalHandler.handle(context);

        } catch (Exception e) {
            logger.error("Request processing error", e);
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 发送HTTP响应
     * 
     * @param ctx Netty通道上下文
     * @param response HTTP响应对象
     */
    private void writeResponse(ChannelHandlerContext ctx, FullHttpResponse response) {
        ctx.writeAndFlush(response);
    }

    /**
     * 发送错误响应
     * 
     * 当发生错误时（如：路由未找到、服务器错误等），
     * 发送带有错误状态码的响应。
     * 
     * @param ctx Netty通道上下文
     * @param status HTTP错误状态码
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(status.toString(), StandardCharsets.UTF_8)
        );
        ctx.writeAndFlush(response);
    }

    /**
     * 处理通道异常
     * 
     * 当通道发生异常时，记录错误日志并关闭通道。
     * 
     * @param ctx Netty通道上下文
     * @param cause 异常原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Channel exception", cause);
        ctx.close();
    }
} 