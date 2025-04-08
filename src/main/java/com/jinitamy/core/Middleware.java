package com.jinitamy.core;

/**
 * 中间件接口
 * 
 * 该接口定义了中间件的标准方法，用于实现请求的预处理和后处理逻辑。
 * 中间件按照添加顺序依次执行，可以用于：
 * 1. 请求预处理（如：参数验证、权限检查）
 * 2. 日志记录
 * 3. 性能监控
 * 4. 响应后处理
 * 
 * 使用示例：
 * <pre>
 * // 日志中间件
 * engine.use((ctx, next) -> {
 *     long start = System.currentTimeMillis();
 *     next.handle(ctx);
 *     long duration = System.currentTimeMillis() - start;
 *     System.out.println("Request processed in " + duration + "ms");
 * });
 * 
 * // 认证中间件
 * engine.use((ctx, next) -> {
 *     String token = ctx.getHeaders().get("Authorization");
 *     if (token == null || !isValidToken(token)) {
 *         ctx.status(401);
 *         return;
 *     }
 *     next.handle(ctx);
 * });
 * </pre>
 * 
 * 注意事项：
 * 1. 中间件必须调用next.handle(ctx)来继续处理链
 * 2. 中间件可以决定是否继续处理链（如：认证失败时）
 * 3. 中间件应该正确处理异常，避免异常传播到框架层面
 */
@FunctionalInterface
public interface Middleware {
    /**
     * 处理请求
     * 
     * 该方法在请求处理链中被调用，可以：
     * 1. 在调用next.handle(ctx)之前进行预处理
     * 2. 在调用next.handle(ctx)之后进行后处理
     * 3. 决定是否继续处理链
     * 
     * @param ctx 请求上下文，包含请求和响应的所有信息
     * @param next 下一个处理器，用于继续处理链
     * @throws Exception 处理过程中可能抛出的异常
     */
    void handle(Context ctx, Handler next) throws Exception;
} 