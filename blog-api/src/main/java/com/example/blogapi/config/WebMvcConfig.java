package com.example.blogapi.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.example.blogapi.handler.LoginInterceptor;
import com.example.blogapi.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //跨域配置
//        registry.addMapping("/**").allowedOrigins("http://localhost:8081")
//                .allowedOriginPatterns("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(loginInterceptor).addPathPatterns("/test").
               addPathPatterns("/comments/create/change")
               .addPathPatterns("/articles/publish").addPathPatterns("/document/**");
       //.addPathPatterns("/document/*")
       //registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/login").
               //excludePathPatterns("/register");
    }

//    @Bean
//    public HttpMessageConverters fastJsonHttpMessageConverters() {
//        // 创建FastJson信息转换对象
//        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
//        // 创建配置类
//        FastJsonConfig config = new FastJsonConfig();
//        // 创建序列化处理对象
//        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
//        // 将Long类型转换为String类型
//        serializeConfig.put(Long.class, ToStringSerializer.instance);
//        // 将配置添加到序列化处理对象中
//        config.setSerializeConfig(serializeConfig);
//        // 配置转换格式
//        converter.setFastJsonConfig(config);
//        // 将FastJson添加到视图消息转换器列表内
//        return new HttpMessageConverters(converter);
//    }
}
