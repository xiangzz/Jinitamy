package com.jinitamy.core;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HttpHandlerTest {
    private Engine engine;
    private HttpHandler handler;
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        handler = new HttpHandler(engine);
        channel = new EmbeddedChannel(handler);
    }

    @Test
    void testHandleRequest() {
        // 注册一个简单的路由
        engine.get("/test", ctx -> ctx.status(200));

        // 创建测试请求
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/test",
            Unpooled.EMPTY_BUFFER
        );

        // 发送请求
        channel.writeInbound(request);

        // 获取响应
        FullHttpResponse response = channel.readOutbound();
        
        assertNotNull(response, "应该返回响应");
        assertEquals(HttpResponseStatus.OK, response.status(), "应该返回200状态码");
    }

    @Test
    void testHandleNotFound() {
        // 创建一个未注册路由的请求
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/not-found",
            Unpooled.EMPTY_BUFFER
        );

        // 发送请求
        channel.writeInbound(request);

        // 获取响应
        FullHttpResponse response = channel.readOutbound();
        
        assertNotNull(response, "应该返回响应");
        assertEquals(HttpResponseStatus.NOT_FOUND, response.status(), "未找到的路由应该返回404");
    }

    @Test
    void testHandleWithMiddleware() {
        StringBuilder order = new StringBuilder();

        // 添加测试中间件
        engine.use((ctx, next) -> {
            order.append("before");
            next.handle(ctx);
            order.append("after");
        });

        // 注册路由
        engine.get("/test", ctx -> {
            order.append("handler");
            ctx.status(200);
        });

        // 创建请求
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/test",
            Unpooled.EMPTY_BUFFER
        );

        // 发送请求
        channel.writeInbound(request);

        // 获取响应
        FullHttpResponse response = channel.readOutbound();
        
        assertNotNull(response, "应该返回响应");
        assertEquals(HttpResponseStatus.OK, response.status(), "应该返回200状态码");
        assertEquals("beforehandlerafter", order.toString(), "中间件应该按正确顺序执行");
    }

    @Test
    void testHandleError() {
        // 注册一个抛出异常的路由
        engine.get("/error", ctx -> {
            throw new RuntimeException("测试异常");
        });

        // 创建请求
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/error",
            Unpooled.EMPTY_BUFFER
        );

        // 发送请求
        channel.writeInbound(request);

        // 获取响应
        FullHttpResponse response = channel.readOutbound();
        
        assertNotNull(response, "应该返回响应");
        assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, response.status(), "异常应该返回500状态码");
    }

    @Test
    void testHandleWithParams() {
        // 注册带参数的路由
        engine.get("/users/:id", ctx -> {
            String id = ctx.getParam("id");
            ctx.setAttribute("userId", id);
            ctx.status(200);
        });

        // 创建请求
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/users/123",
            Unpooled.EMPTY_BUFFER
        );

        // 发送请求
        channel.writeInbound(request);

        // 获取响应
        FullHttpResponse response = channel.readOutbound();
        
        assertNotNull(response, "应该返回响应");
        assertEquals(HttpResponseStatus.OK, response.status(), "应该返回200状态码");
    }

    @Test
    void testHandleWithHeaders() {
        // 注册检查请求头的路由
        engine.get("/headers", ctx -> {
            String customHeader = ctx.getHeaders().get("X-Custom-Header");
            if ("test-value".equals(customHeader)) {
                ctx.status(200);
            } else {
                ctx.status(400);
            }
        });

        // 创建带自定义请求头的请求
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/headers",
            Unpooled.EMPTY_BUFFER
        );
        request.headers().set("X-Custom-Header", "test-value");

        // 发送请求
        channel.writeInbound(request);

        // 获取响应
        FullHttpResponse response = channel.readOutbound();
        
        assertNotNull(response, "应该返回响应");
        assertEquals(HttpResponseStatus.OK, response.status(), "应该返回200状态码");
    }
} 