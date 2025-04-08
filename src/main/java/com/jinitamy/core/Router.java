package com.jinitamy.core;

import io.netty.handler.codec.http.HttpMethod;

import java.util.*;

/**
 * 路由器类
 * 
 * 实现了基于前缀树的路由匹配系统，支持：
 * 1. 静态路由匹配
 * 2. 动态参数路由（如：/user/:id）
 * 3. 通配符路由（如：/files/*）
 * 
 * 路由匹配优先级：
 * 1. 静态路由
 * 2. 动态参数路由
 * 3. 通配符路由
 */
public class Router {
    /**
     * 路由树节点
     * 用于构建前缀树结构，支持动态参数和通配符
     */
    private static class Node {
        /** 路径片段 */
        String part;
        /** 参数名（如果是动态参数） */
        String param;
        /** 是否为通配符节点 */
        boolean isWild;
        /** 子节点映射 */
        Map<String, Node> children;
        /** 请求处理器 */
        Handler handler;

        /**
         * 创建路由节点
         * 
         * @param part 路径片段
         */
        Node(String part) {
            this.part = part;
            // 判断是否为动态参数（以:开头）或通配符（*）
            this.isWild = part.startsWith(":") || part.equals("*");
            // 提取参数名（去掉:前缀）
            this.param = isWild ? part.substring(1) : "";
            this.children = new HashMap<>();
        }
    }

    /** 路由树根节点映射，按HTTP方法分类 */
    private final Map<HttpMethod, Node> roots;

    /**
     * 构造函数
     * 初始化路由树根节点映射
     */
    public Router() {
        this.roots = new HashMap<>();
    }

    /**
     * 添加路由规则
     * 
     * 将路由规则添加到路由树中，支持动态参数和通配符。
     * 例如：
     * - /user/:id
     * - /files/*
     * - /static/css
     * 
     * @param method HTTP请求方法
     * @param pattern URL匹配模式
     * @param handler 请求处理器
     */
    public void addRoute(HttpMethod method, String pattern, Handler handler) {
        // 解析路径为片段数组
        String[] parts = parsePath(pattern);
        
        // 获取或创建对应HTTP方法的路由树根节点
        Node root = roots.computeIfAbsent(method, k -> new Node(""));
        
        // 构建路由树
        Node node = root;
        for (String part : parts) {
            node = node.children.computeIfAbsent(part, Node::new);
        }
        node.handler = handler;
    }

    /**
     * 查找匹配的路由处理器
     * 
     * 根据HTTP方法和请求路径查找匹配的路由规则，
     * 如果匹配到动态参数，会将其值保存到上下文中。
     * 
     * @param method HTTP请求方法
     * @param path 请求路径
     * @param ctx 请求上下文
     * @return 匹配的处理器，如果没有匹配则返回null
     */
    public Handler getRoute(HttpMethod method, String path, Context ctx) {
        String[] parts = parsePath(path);
        Node root = roots.get(method);
        if (root == null) {
            return null;
        }

        Node node = root;
        for (String part : parts) {
            boolean found = false;
            // 遍历所有子节点，包括动态参数和通配符
            for (Map.Entry<String, Node> entry : new ArrayList<>(node.children.entrySet())) {
                Node child = entry.getValue();
                if (child.part.equals(part) || child.isWild) {
                    // 如果是动态参数，保存参数值
                    if (child.isWild && !child.param.isEmpty()) {
                        ctx.setParam(child.param, part);
                    }
                    node = child;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }
        
        return node.handler;
    }

    /**
     * 解析URL路径
     * 
     * 将URL路径分割为片段数组，过滤掉空片段。
     * 例如："/user/123" -> ["user", "123"]
     * 
     * @param path URL路径
     * @return 路径片段数组
     */
    private String[] parsePath(String path) {
        return Arrays.stream(path.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);
    }
} 