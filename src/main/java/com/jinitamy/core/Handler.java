package com.jinitamy.core;

/**
 * 路由处理器接口
 * 
 * 该接口定义了处理HTTP请求的标准方法，用于实现具体的业务逻辑。
 * 由于使用了@FunctionalInterface注解，可以使用Lambda表达式简化实现。
 * 
 * 使用示例：
 * <pre>
 * // 使用Lambda表达式
 * engine.get("/", ctx -> {
 *     ctx.getResponse().content().writeBytes("Hello World".getBytes());
 * });
 * 
 * // 使用匿名类
 * engine.get("/user", new Handler() {
 *     @Override
 *     public void handle(Context ctx) throws Exception {
 *         // 处理逻辑
 *     }
 * });
 * </pre>
 * 
 * 注意事项：
 * 1. 处理器应该正确处理异常，避免异常传播到框架层面
 * 2. 处理器应该设置适当的响应状态码和响应体
 * 3. 处理器可以通过Context对象访问请求和响应信息
 */
@FunctionalInterface
public interface Handler {
    /**
     * 处理HTTP请求
     * 
     * 该方法在路由匹配成功后被调用，用于处理具体的业务逻辑。
     * 可以通过Context对象：
     * 1. 获取请求信息（方法、路径、参数等）
     * 2. 设置响应信息（状态码、响应体等）
     * 3. 访问自定义属性
     * 
     * @param ctx 请求上下文，包含请求和响应的所有信息
     * @throws Exception 处理过程中可能抛出的异常
     */
    void handle(Context ctx) throws Exception;
} 