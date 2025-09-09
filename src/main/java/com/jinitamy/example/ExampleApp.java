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
                System.err.println("处理请求时发生错误: " + e.getMessage());
                ctx.status(500);
            }
            System.out.printf("耗时: %dms\n", System.currentTimeMillis() - start);
        });

        // 注册路由
        engine.get("/", ctx -> {
            String resp = "Hello, Jinitamy!";
            ctx.getResponse().headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .set(HttpHeaderNames.CONTENT_LENGTH, resp.length());
            ctx.getResponse().content()
                    .writeBytes(Unpooled.copiedBuffer(resp, StandardCharsets.UTF_8));
        });

        engine.get("/hello/:name", ctx -> {
            String name = ctx.getParam("name");
            if (name == null || name.trim().isEmpty()) {
                ctx.status(400);
                String errorResp = "错误请求: 需要提供name参数";
                ctx.getResponse().headers()
                        .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                        .set(HttpHeaderNames.CONTENT_LENGTH, errorResp.length());
                ctx.getResponse().content()
                        .writeBytes(Unpooled.copiedBuffer(errorResp, StandardCharsets.UTF_8));
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
            System.out.println("\n正在关闭服务器...");
            try {
                engine.stop();
                System.out.println("服务器已优雅关闭。");
            } catch (Exception e) {
                System.err.println("服务器关闭时发生错误: " + e.getMessage());
            }
        }));

        System.out.println("服务器正在8889端口启动...");
        System.out.println("按 Ctrl+C 停止服务器");
        engine.start();
    }
}
