package com.example.config;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Created by SunYi on 2016/2/1/0001.
 */
@Configuration
@EnableWebMvc
//扫描包需要添加该配置文件影响到的文件夹，*代表全部，若单独列举用，分割。MVC影响到扫描到controller所在文件夹
@ComponentScan(basePackages = "com.example.controller")
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    //声明页面的编码格式、类型
    private static final String CONTENTTYPE = "text/html; charset=UTF-8";

    //Thymeleaf框架配置
    @Bean
    public TemplateResolver templateResolver(){
        ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
        //设置模板位置，和模板后缀
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        //去除缓存
        resolver.setCacheable(false);
        resolver.setCharacterEncoding("UTF-8");
        //设置末班模式，这样不会严格按照XHTML来检测
        resolver.setTemplateMode("LEGACYHTML5");

        return resolver;
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        将资源文件定位到web根目录
        registry.addResourceHandler("/**").addResourceLocations("/static");
    }
    @Bean
    public SpringTemplateEngine templateEngine(){

        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.setTemplateResolver(templateResolver());
        springTemplateEngine.addDialect(new LayoutDialect());

        return springTemplateEngine;
    }

    /**
     * 模板引擎解释器
     * @return
     */
    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setContentType(CONTENTTYPE);
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setOrder(1);
        return viewResolver;
    }

}