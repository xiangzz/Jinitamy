package com.jinitamy.core;

import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouterTest {
    private Router router;

    @BeforeEach
    void setUp() {
        router = new Router();
    }

    @Test
    void testAddAndGetStaticRoute() {
        // 设置一个处理器，返回固定的响应码
        Handler handler = ctx -> ctx.status(200);
        router.addRoute(HttpMethod.GET, "/hello", handler);

        // 创建上下文对象
        Context ctx = new Context(null, null);
        
        // 测试匹配已注册的路由
        Handler foundHandler = router.getRoute(HttpMethod.GET, "/hello", ctx);
        assertNotNull(foundHandler, "应该找到注册的路由处理器");
        assertEquals(handler, foundHandler, "应该返回正确的处理器");

        // 测试未注册的路由
        Handler notFoundHandler = router.getRoute(HttpMethod.GET, "/not-exist", ctx);
        assertNull(notFoundHandler, "未注册的路由应该返回null");
    }

    @Test
    void testDynamicRoute() {
        Handler handler = ctx -> ctx.status(200);
        router.addRoute(HttpMethod.GET, "/users/:id", handler);

        Context ctx = new Context(null, null);
        Handler foundHandler = router.getRoute(HttpMethod.GET, "/users/123", ctx);
        
        assertNotNull(foundHandler, "应该找到动态路由处理器");
        assertEquals("123", ctx.getParam("id"), "应该正确解析路由参数");
    }

    @Test
    void testMultipleHttpMethods() {
        Handler getHandler = ctx -> ctx.status(200);
        Handler postHandler = ctx -> ctx.status(201);

        router.addRoute(HttpMethod.GET, "/api", getHandler);
        router.addRoute(HttpMethod.POST, "/api", postHandler);

        Context ctx = new Context(null, null);
        
        Handler foundGetHandler = router.getRoute(HttpMethod.GET, "/api", ctx);
        Handler foundPostHandler = router.getRoute(HttpMethod.POST, "/api", ctx);
        
        assertNotNull(foundGetHandler, "应该找到GET方法的处理器");
        assertNotNull(foundPostHandler, "应该找到POST方法的处理器");
        assertNotEquals(foundGetHandler, foundPostHandler, "GET和POST处理器应该不同");
    }

    @Test
    void testNestedRoute() {
        Handler handler = ctx -> ctx.status(200);
        router.addRoute(HttpMethod.GET, "/api/users/:id/posts/:postId", handler);

        Context ctx = new Context(null, null);
        Handler foundHandler = router.getRoute(HttpMethod.GET, "/api/users/123/posts/456", ctx);
        
        assertNotNull(foundHandler, "应该找到嵌套的动态路由处理器");
        assertEquals("123", ctx.getParam("id"), "应该正确解析用户ID参数");
        assertEquals("456", ctx.getParam("postId"), "应该正确解析帖子ID参数");
    }

    @Test
    void testWildcardRoute() {
        Handler handler = ctx -> ctx.status(200);
        router.addRoute(HttpMethod.GET, "/files/*", handler);

        Context ctx = new Context(null, null);
        Handler foundHandler = router.getRoute(HttpMethod.GET, "/files/documents/report.pdf", ctx);
        
        assertNotNull(foundHandler, "应该找到通配符路由处理器");
    }

    @Test
    void testMethodNotAllowed() {
        Handler handler = ctx -> ctx.status(200);
        router.addRoute(HttpMethod.GET, "/api/users", handler);

        Context ctx = new Context(null, null);
        Handler foundHandler = router.getRoute(HttpMethod.POST, "/api/users", ctx);
        
        assertNull(foundHandler, "不允许的HTTP方法应该返回null");
    }

    @Test
    void testEmptyPath() {
        Handler handler = ctx -> ctx.status(200);
        router.addRoute(HttpMethod.GET, "/", handler);

        Context ctx = new Context(null, null);
        Handler foundHandler = router.getRoute(HttpMethod.GET, "/", ctx);
        
        assertNotNull(foundHandler, "应该找到根路径的处理器");
    }

    @Test
    void testMultipleParamsInSamePath() {
        Handler handler = ctx -> ctx.status(200);
        router.addRoute(HttpMethod.GET, "/:version/api/:resource/:id", handler);

        Context ctx = new Context(null, null);
        Handler foundHandler = router.getRoute(HttpMethod.GET, "/v1/api/users/123", ctx);
        
        assertNotNull(foundHandler, "应该找到多参数路由处理器");
        assertEquals("v1", ctx.getParam("version"), "应该正确解析version参数");
        assertEquals("users", ctx.getParam("resource"), "应该正确解析resource参数");
        assertEquals("123", ctx.getParam("id"), "应该正确解析id参数");
    }
} 