# Jinitamy Web Framework

Jinitamy是一个轻量级的Java Web框架，基于Netty构建，提供简单直观的API。

## 特性

- 基于Netty的高性能HTTP服务器
- 简单直观的路由系统
- 支持动态路由参数
- 灵活的中间件机制
- 链式处理流程
- 内置FreeMarker模板引擎支持

## 环境要求

- Java 17或更高版本
- Maven 3.6或更高版本

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.jinitamy</groupId>
    <artifactId>jinitamy</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 创建应用

```java
import com.jinitamy.core.Engine;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) throws Exception {
        Engine engine = new Engine();
        
        // 添加路由
        engine.get("/", ctx -> {
            String resp = "Hello, Jinitamy!";
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
```

### 动态路由

```java
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
```

### 使用模板引擎

1. 创建模板文件 (src/main/resources/templates/index.ftl):

```html
<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
</head>
<body>
    <h1>${title}</h1>
    <div class="content">
        ${content}
    </div>
</body>
</html>
```

2. 在路由中使用模板：

```java
import com.jinitamy.core.template.TemplateEngine;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

engine.get("/", ctx -> {
    Map<String, Object> model = new HashMap<>();
    model.put("title", "欢迎页面");
    model.put("content", "这是使用模板引擎渲染的内容");
    
    String html = TemplateEngine.render("index.ftl", model);
    ctx.getResponse().headers()
            .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML)
            .set(HttpHeaderNames.CONTENT_LENGTH, html.length());
    ctx.getResponse().content().writeBytes(Unpooled.copiedBuffer(html, StandardCharsets.UTF_8));
});
```

### 添加中间件

```java
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
```

## 项目依赖

- Netty 4.1.94.Final - 网络应用框架
- FreeMarker 2.3.32 - 模板引擎
- SLF4J 2.0.9 - 日志门面
- Logback 1.4.11 - 日志实现
- JUnit Jupiter 5.10.0 - 测试框架

## 构建项目

```bash
mvn clean package
```

## 运行示例

### 方法1：直接运行主类
```bash
mvn exec:java -Dexec.mainClass="com.jinitamy.example.ExampleApp"
```

### 方法2：编译后运行
```bash
mvn compile
java -cp target/classes com.jinitamy.example.ExampleApp
```

## 目录结构

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── jinitamy/
│   │           ├── core/
│   │           │   ├── Engine.java
│   │           │   ├── Router.java
│   │           │   ├── Context.java
│   │           │   ├── Handler.java
│   │           │   ├── Middleware.java
│   │           │   ├── HttpHandler.java
│   │           │   └── template/
│   │           │       └── TemplateEngine.java
│   │           └── example/
│   │               └── ExampleApp.java
│   └── resources/
│       └── templates/
│           └── index.ftl
└── test/
    └── java/
        └── com/
            └── jinitamy/
                └── core/
                    ├── RouterTest.java
                    ├── ContextTest.java
                    └── ...
```

## 许可证

MIT License 