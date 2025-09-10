package com.jinitamy.example;

import com.jinitamy.core.Engine;
import com.jinitamy.core.template.TemplateEngine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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

        /* 注册路由 */
        // 使用模板引擎的示例
        engine.get("/", ctx -> {
            Map<String, Object> model = new HashMap<>();
            model.put("title", "欢迎使用 Jinitamy 框架");
            model.put("content", "这是一个基于Netty构建的轻量级Java Web框架，支持路由、中间件和模板渲染。");
            // 1. 渲染模板
            String html = TemplateEngine.render("index.ftl", model);
            // 2. 按 UTF-8 拿到真实字节数组
            byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
            // 3. 构造 ByteBuf（零拷贝）
            ByteBuf buf = Unpooled.wrappedBuffer(htmlBytes);   // 只包装，不复制
            // 4. 设置响应头（关键：用字节长度，不是字符长度）
            ctx.getResponse().headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML + "; charset=utf-8")
                    .set(HttpHeaderNames.CONTENT_LENGTH, htmlBytes.length)   // ← 正解
                    .set(HttpHeaderNames.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .set(HttpHeaderNames.PRAGMA, "no-cache")
                    .set(HttpHeaderNames.EXPIRES, "0");
            // 5. 写出内容（自动释放）
            ctx.getResponse().content().writeBytes(buf);
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

            Map<String, Object> model = new HashMap<>();
            model.put("title", "Hello");
            model.put("content", "Hello, " + name + " !");

            String html = TemplateEngine.render("index.ftl", model);
            byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
            ByteBuf buf = Unpooled.wrappedBuffer(htmlBytes);   // 只包装，不复制

            ctx.getResponse().headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML)
                    .set(HttpHeaderNames.CONTENT_LENGTH, htmlBytes.length)  
                    .set(HttpHeaderNames.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .set(HttpHeaderNames.PRAGMA, "no-cache")
                    .set(HttpHeaderNames.EXPIRES, "0");
            ctx.getResponse().content().writeBytes(buf);
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
