package com.jinitamy.core;

import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EngineTest {
    private Engine engine;

    @BeforeEach
    void setUp() {
        engine = new Engine();
    }

    @Test
    void testDefaultPort() {
        assertEquals(8080, engine.getPort(), "默认端口应该是8080");
    }

    @Test
    void testSetPort() {
        engine.setPort(9000);
        assertEquals(9000, engine.getPort(), "应该能够设置自定义端口");
    }

    @Test
    void testRouteRegistration() {
        Handler handler = ctx -> ctx.status(200);
        
        engine.get("/test", handler);
        engine.post("/test", handler);

        Router router = engine.getRouter();
        Context ctx = new Context(null, null);

        Handler getHandler = router.getRoute(HttpMethod.GET, "/test", ctx);
        Handler postHandler = router.getRoute(HttpMethod.POST, "/test", ctx);

        assertNotNull(getHandler, "GET路由应该被正确注册");
        assertNotNull(postHandler, "POST路由应该被正确注册");
    }

    @Test
    void testMiddlewareRegistration() {
        Middleware middleware1 = (ctx, next) -> next.handle(ctx);
        Middleware middleware2 = (ctx, next) -> next.handle(ctx);

        engine.use(middleware1);
        engine.use(middleware2);

        assertEquals(2, engine.getMiddlewares().size(), "中间件应该被正确注册");
        assertTrue(engine.getMiddlewares().contains(middleware1), "应该包含第一个中间件");
        assertTrue(engine.getMiddlewares().contains(middleware2), "应该包含第二个中间件");
    }

    @Test
    void testMiddlewareOrder() {
        StringBuilder order = new StringBuilder();
        
        Middleware middleware1 = (ctx, next) -> {
            order.append("1");
            next.handle(ctx);
            order.append("1");
        };
        
        Middleware middleware2 = (ctx, next) -> {
            order.append("2");
            next.handle(ctx);
            order.append("2");
        };

        engine.use(middleware1);
        engine.use(middleware2);

        Handler handler = ctx -> order.append("H");
        engine.get("/test", handler);

        Context ctx = new Context(null, null);
        Handler finalHandler = engine.getRouter().getRoute(HttpMethod.GET, "/test", ctx);
        
        try {
            // 构建中间件链
            Handler chainedHandler = (Context c) -> {
                finalHandler.handle(c);
            };

            for (int i = engine.getMiddlewares().size() - 1; i >= 0; i--) {
                Middleware m = engine.getMiddlewares().get(i);
                Handler next = chainedHandler;
                chainedHandler = (Context c) -> m.handle(c, next);
            }

            chainedHandler.handle(ctx);
            
            assertEquals("12H21", order.toString(), "中间件应该按照正确的顺序执行");
        } catch (Exception e) {
            fail("中间件执行不应抛出异常");
        }
    }

    @Test
    void testRouterAccess() {
        assertNotNull(engine.getRouter(), "应该能够访问路由器实例");
    }

    @Test
    void testMiddlewaresAccess() {
        assertNotNull(engine.getMiddlewares(), "应该能够访问中间件列表");
        assertTrue(engine.getMiddlewares().isEmpty(), "初始中间件列表应该为空");
    }
} 