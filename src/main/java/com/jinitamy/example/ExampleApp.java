package com.jinitamy.example;

import com.jinitamy.core.Engine;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.nio.charset.StandardCharsets;

public class ExampleApp {
    public static void main(String[] args) throws Exception {
        Engine engine = new Engine();
        
        // 添加日志中间件
        engine.use((ctx, next) -> {
            long start = System.currentTimeMillis();
            System.out.printf("[%s] %s\n", ctx.getMethod(), ctx.getPath());
            try {
                next.handle(ctx);
            } catch (Exception e) {
                System.err.println("Error processing request: " + e.getMessage());
                ctx.status(500);
            }
            System.out.printf("Time: %dms\n", System.currentTimeMillis() - start);
        });

        // 注册路由
        engine.get("/", ctx -> {
            String resp = "Hello, Jinitamy!";
            ctx.getResponse().headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .set(HttpHeaderNames.CONTENT_LENGTH, resp.length());
            ctx.getResponse().content().writeBytes(Unpooled.copiedBuffer(resp, StandardCharsets.UTF_8));
        });

        engine.get("/hello/:name", ctx -> {
            String name = ctx.getParam("name");
            if (name == null || name.trim().isEmpty()) {
                ctx.status(400);
                String errorResp = "Bad Request: name parameter is required";
                ctx.getResponse().headers()
                        .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                        .set(HttpHeaderNames.CONTENT_LENGTH, errorResp.length());
                ctx.getResponse().content().writeBytes(Unpooled.copiedBuffer(errorResp, StandardCharsets.UTF_8));
                return;
            }
            
            String resp = "Hello, " + name + "!";
            ctx.getResponse().headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .set(HttpHeaderNames.CONTENT_LENGTH, resp.length());
            ctx.getResponse().content().writeBytes(Unpooled.copiedBuffer(resp, StandardCharsets.UTF_8));
        });

        // 启动服务器
        engine.setPort(8889);
        
        // 添加优雅关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            // 注意：这里需要Engine提供stop方法才能实现真正的优雅关闭
            // 目前Engine没有stop方法，所以这里只是打印信息
        }));
        
        System.out.println("Server starting on port 8889...");
        System.out.println("Press Ctrl+C to stop the server");
        engine.start();
    }
} 