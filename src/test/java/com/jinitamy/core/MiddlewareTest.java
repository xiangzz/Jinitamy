package com.jinitamy.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MiddlewareTest {
    
    @Test
    void testMiddlewareExecution() throws Exception {
        StringBuilder order = new StringBuilder();
        
        // 创建中间件
        Middleware middleware = (ctx, next) -> {
            order.append("before");
            next.handle(ctx);
            order.append("after");
        };
        
        // 创建处理器
        Handler handler = ctx -> order.append("handler");
        
        Context context = new Context(null, null);
        middleware.handle(context, handler);
        
        assertEquals("beforehandlerafter", order.toString(), "中间件应该按照正确的顺序执行");
    }
    
    @Test
    void testMiddlewareChain() throws Exception {
        StringBuilder order = new StringBuilder();
        
        // 创建多个中间件
        Middleware first = (ctx, next) -> {
            order.append("1");
            next.handle(ctx);
            order.append("1");
        };
        
        Middleware second = (ctx, next) -> {
            order.append("2");
            next.handle(ctx);
            order.append("2");
        };
        
        // 创建处理器
        Handler handler = ctx -> order.append("H");
        
        Context context = new Context(null, null);
        
        // 构建并执行中间件链
        Handler chainedHandler = (Context c) -> {
            handler.handle(c);
        };
        
        chainedHandler = wrapWithMiddleware(chainedHandler, second);
        chainedHandler = wrapWithMiddleware(chainedHandler, first);
        
        chainedHandler.handle(context);
        
        assertEquals("12H21", order.toString(), "中间件链应该按照正确的顺序执行");
    }
    
    @Test
    void testMiddlewareException() {
        // 创建一个抛出异常的中间件
        Middleware middleware = (ctx, next) -> {
            throw new RuntimeException("中间件异常");
        };
        
        Handler handler = ctx -> {};
        Context context = new Context(null, null);
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            middleware.handle(context, handler);
        });
        
        assertEquals("中间件异常", exception.getMessage());
    }
    
    @Test
    void testMiddlewareWithContext() throws Exception {
        // 创建一个修改上下文的中间件
        Middleware middleware = (ctx, next) -> {
            ctx.setAttribute("beforeKey", "before");
            next.handle(ctx);
            ctx.setAttribute("afterKey", "after");
        };
        
        Handler handler = ctx -> {
            ctx.setAttribute("handlerKey", "handler");
        };
        
        Context context = new Context(null, null);
        middleware.handle(context, handler);
        
        assertEquals("before", context.getAttribute("beforeKey"), "中间件应该在处理器之前设置属性");
        assertEquals("handler", context.getAttribute("handlerKey"), "处理器应该设置属性");
        assertEquals("after", context.getAttribute("afterKey"), "中间件应该在处理器之后设置属性");
    }
    
    @Test
    void testMiddlewareSkipNext() throws Exception {
        StringBuilder order = new StringBuilder();
        
        // 创建一个不调用next的中间件
        Middleware middleware = (ctx, next) -> {
            order.append("middleware");
            // 不调用next.handle()
        };
        
        Handler handler = ctx -> order.append("handler");
        
        Context context = new Context(null, null);
        middleware.handle(context, handler);
        
        assertEquals("middleware", order.toString(), "中间件应该能够跳过后续处理器");
    }
    
    // 辅助方法：包装处理器与中间件
    private Handler wrapWithMiddleware(Handler handler, Middleware middleware) {
        return (Context ctx) -> middleware.handle(ctx, handler);
    }
} 