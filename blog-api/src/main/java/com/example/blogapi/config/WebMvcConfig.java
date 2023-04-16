package com.example.blogapi.config;

import com.example.blogapi.handler.LoginInterceptor;
import com.example.blogapi.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
               .addPathPatterns("/articles/publish").addPathPatterns("/document/*");
       //registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/login").
               //excludePathPatterns("/register");
    }

}
