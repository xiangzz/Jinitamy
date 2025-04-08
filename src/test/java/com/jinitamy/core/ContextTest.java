package com.jinitamy.core;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContextTest {
    private Context context;
    private FullHttpRequest request;
    private FullHttpResponse response;

    @BeforeEach
    void setUp() {
        request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/test",
            Unpooled.EMPTY_BUFFER
        );
        response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.EMPTY_BUFFER
        );
        context = new Context(request, response);
    }

    @Test
    void testPath() {
        assertEquals("/test", context.getPath(), "路径应该与请求URI匹配");
        
        context.setPath("/new-path");
        assertEquals("/new-path", context.getPath(), "应该能够更新路径");
    }

    @Test
    void testMethod() {
        assertEquals(HttpMethod.GET, context.getMethod(), "HTTP方法应该与请求方法匹配");
    }

    @Test
    void testHeaders() {
        request.headers().set("X-Custom-Header", "test-value");
        assertEquals("test-value", context.getHeaders().get("X-Custom-Header"), "应该能够获取请求头");
    }

    @Test
    void testParams() {
        context.setParam("id", "123");
        assertEquals("123", context.getParam("id"), "应该能够设置和获取路由参数");
        
        assertNull(context.getParam("non-existent"), "不存在的参数应该返回null");
    }

    @Test
    void testAttributes() {
        Object value = new Object();
        context.setAttribute("key", value);
        assertEquals(value, context.getAttribute("key"), "应该能够设置和获取属性");
        
        assertNull(context.getAttribute("non-existent"), "不存在的属性应该返回null");
    }

    @Test
    void testStatus() {
        context.status(404);
        assertEquals(HttpResponseStatus.NOT_FOUND, response.status(), "应该能够设置响应状态码");
        
        context.status(200);
        assertEquals(HttpResponseStatus.OK, response.status(), "应该能够更新响应状态码");
    }

    @Test
    void testNullRequest() {
        Context nullContext = new Context(null, response);
        assertEquals("", nullContext.getPath(), "空请求时应返回空路径");
    }

    @Test
    void testNullResponse() {
        Context nullContext = new Context(request, null);
        // 不应抛出异常
        nullContext.status(200);
    }
} 