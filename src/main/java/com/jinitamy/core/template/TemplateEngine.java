package com.jinitamy.core.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * FreeMarker模板引擎封装类
 * 
 * 该类提供了对FreeMarker模板引擎的简单封装，用于处理服务端渲染。
 * 支持从指定目录加载模板文件，并使用UTF-8编码进行渲染。
 * 
 * 使用示例：
 * <pre>
 * Map<String, Object> model = new HashMap<>();
 * model.put("title", "页面标题");
 * model.put("content", "页面内容");
 * String html = TemplateEngine.render("index.ftl", model);
 * </pre>
 * 
 * 注意事项：
 * 1. 模板文件必须放在 src/main/resources/templates 目录下
 * 2. 模板文件必须使用.ftl后缀
 * 3. 模板文件必须使用UTF-8编码
 * 4. 模板变量使用${变量名}的形式
 */
public class TemplateEngine {
    /**
     * FreeMarker配置对象
     * 使用静态初始化确保全局单例
     */
    private static final Configuration configuration;

    /**
     * 静态初始化块
     * 配置FreeMarker模板引擎的基本设置：
     * 1. 设置模板文件加载目录
     * 2. 设置默认编码为UTF-8
     * 
     * @throws RuntimeException 当模板引擎初始化失败时抛出
     */
    static {
        configuration = new Configuration(Configuration.VERSION_2_3_32);
        try {
            // 设置模板文件所在目录
            configuration.setDirectoryForTemplateLoading(
                new File("src/main/resources/templates")
            );
            // 设置模板文件编码
            configuration.setDefaultEncoding("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize template engine", e);
        }
    }

    /**
     * 渲染模板文件
     * 
     * 根据模板名称和数据模型，渲染对应的模板文件。
     * 模板文件将从配置的目录中加载，并使用UTF-8编码处理。
     * 
     * @param templateName 模板文件名（不含路径，例如："index.ftl"）
     * @param model 数据模型，包含模板中使用的变量
     * @return 渲染后的HTML内容
     * @throws RuntimeException 当模板文件不存在或渲染过程发生错误时抛出
     */
    public static String render(String templateName, Map<String, Object> model) {
        try {
            // 加载模板文件
            Template template = configuration.getTemplate(templateName);
            // 创建输出写入器
            StringWriter writer = new StringWriter();
            // 处理模板并输出结果
            template.process(model, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Template rendering failed", e);
        }
    }
} 