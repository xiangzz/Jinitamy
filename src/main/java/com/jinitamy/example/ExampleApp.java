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
            next.handle(ctx);
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
            String resp = "Hello, " + name + "!";
            ctx.getResponse().headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .set(HttpHeaderNames.CONTENT_LENGTH, resp.length());
            ctx.getResponse().content().writeBytes(Unpooled.copiedBuffer(resp, StandardCharsets.UTF_8));
        });

        // 启动服务器
        engine.setPort(8080);
        engine.start();
    }
} 