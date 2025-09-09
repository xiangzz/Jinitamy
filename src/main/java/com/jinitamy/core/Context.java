package com.jinitamy.core;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求上下文类
 * 
 * 该类封装了HTTP请求和响应的相关信息，提供了统一的接口来访问：
 * 1. 请求信息（方法、路径、头部等）
 * 2. 响应信息（状态码、响应体等）
 * 3. 路由参数（动态路由参数）
 * 4. 自定义属性（用于在中间件和处理器之间传递数据）
 * 
 * 使用示例：
 * <pre>
 * // 获取路由参数
 * String userId = ctx.getParam("id");
 * 
 * // 设置响应状态码
 * ctx.status(200);
 * 
 * // 在中间件中设置属性
 * ctx.setAttribute("user", user);
 * </pre>
 */
public class Context {
    /** HTTP请求对象 */
    private final FullHttpRequest request;
    /** HTTP响应对象 */
    private final FullHttpResponse response;
    /** 路由参数映射 */
    private final Map<String, String> params;
    /** 自定义属性映射 */
    private final Map<String, Object> attributes;
    /** 请求路径 */
    private String path;

    /**
     * 创建请求上下文
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    public Context(FullHttpRequest request, FullHttpResponse response) {
        this.request = request;
        this.response = response;
        this.params = new HashMap<>();
        this.attributes = new HashMap<>();
        this.path = request != null ? request.uri() : "";
    }

    /**
     * 获取请求路径
     * 
     * @return 当前请求路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置请求路径
     * 
     * @param path 要设置的路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取HTTP请求方法
     * 
     * @return HTTP请求方法（GET、POST等）
     */
    public HttpMethod getMethod() {
        return request.method();
    }

    /**
     * 获取HTTP请求头
     * 
     * @return HTTP请求头对象
     */
    public HttpHeaders getHeaders() {
        return request.headers();
    }

    /**
     * 设置路由参数
     * 
     * 用于存储动态路由中的参数值，例如：
     * 路由模式：/user/:id
     * 实际URL：/user/123
     * 则：ctx.setParam("id", "123")
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void setParam(String key, String value) {
        params.put(key, value);
    }

    /**
     * 获取路由参数
     * 
     * @param key 参数名
     * @return 参数值，如果不存在则返回null
     */
    public String getParam(String key) {
        return params.get(key);
    }

    /**
     * 移除路由参数
     * 
     * 用于路由匹配回溯时清理参数
     * 
     * @param key 参数名
     * @return 被移除的参数值，如果不存在则返回null
     */
    public String removeParam(String key) {
        return params.remove(key);
    }

    /**
     * 设置自定义属性
     * 
     * 用于在中间件和处理器之间传递数据，例如：
     * - 用户认证信息
     * - 请求处理时间
     * - 自定义业务数据
     * 
     * @param key 属性名
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 获取自定义属性
     * 
     * @param key 属性名
     * @return 属性值，如果不存在则返回null
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 获取HTTP请求对象
     * 
     * @return 完整的HTTP请求对象
     */
    public FullHttpRequest getRequest() {
        return request;
    }

    /**
     * 获取HTTP响应对象
     * 
     * @return 完整的HTTP响应对象
     */
    public FullHttpResponse getResponse() {
        return response;
    }

    /**
     * 设置HTTP响应状态码
     * 
     * 设置响应的HTTP状态码，例如：
     * - 200：成功
     * - 404：未找到
     * - 500：服务器错误
     * 
     * @param statusCode HTTP状态码
     * @return 当前上下文实例（支持链式调用）
     */
    public Context status(int statusCode) {
        if (response != null) {
            response.setStatus(HttpResponseStatus.valueOf(statusCode));
        }
        return this;
    }
}