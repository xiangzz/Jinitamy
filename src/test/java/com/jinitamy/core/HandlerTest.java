package com.jinitamy.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandlerTest {
    
    @Test
    void testHandlerExecution() throws Exception {
        // 创建一个简单的处理器
        Handler handler = ctx -> ctx.status(200);
        
        // 创建上下文
        Context context = new Context(null, null);
        
        // 执行处理器
        handler.handle(context);
    }
    
    @Test
    void testHandlerChaining() throws Exception {
        StringBuilder order = new StringBuilder();
        
        // 创建多个处理器形成调用链
        Handler first = ctx -> {
            order.append("1");
            ctx.status(200);
        };
        
        Handler second = ctx -> {
            order.append("2");
            ctx.status(201);
        };
        
        Context context = new Context(null, null);
        
        // 按顺序执行处理器
        first.handle(context);
        second.handle(context);
        
        assertEquals("12", order.toString(), "处理器应该按顺序执行");
    }
    
    @Test
    void testHandlerException() {
        // 创建一个抛出异常的处理器
        Handler handler = ctx -> {
            throw new RuntimeException("测试异常");
        };
        
        Context context = new Context(null, null);
        
        // 验证异常抛出
        Exception exception = assertThrows(RuntimeException.class, () -> {
            handler.handle(context);
        });
        
        assertEquals("测试异常", exception.getMessage());
    }
    
    @Test
    void testHandlerWithAttributes() throws Exception {
        Handler handler = ctx -> {
            ctx.setAttribute("key", "value");
            ctx.status(200);
        };
        
        Context context = new Context(null, null);
        handler.handle(context);
        
        assertEquals("value", context.getAttribute("key"), "处理器应该能够设置上下文属性");
    }
    
    @Test
    void testHandlerWithParams() throws Exception {
        Handler handler = ctx -> {
            ctx.setParam("id", "123");
            ctx.status(200);
        };
        
        Context context = new Context(null, null);
        handler.handle(context);
        
        assertEquals("123", context.getParam("id"), "处理器应该能够设置路由参数");
    }
} 