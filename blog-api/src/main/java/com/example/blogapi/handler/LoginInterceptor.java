package com.example.blogapi.handler;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.LoginService;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.ErrorCode;
import com.example.blogapi.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
@Slf4j             //日志对象,方法
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private  LoginService loginService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       /*1.在执行controller方法（Handler）之前进行执行
       * 2. 判断token是否为空，如果为空返回未登录
       * 3.如果token不为空，登录验证 loginService checkToken
       * 4.如果成功，放行
       * */
        if(! (handler instanceof HandlerMethod))
        {
            //handler 可能是访问静态资源，默认去static目录去查询
            return  true;
        }
        String token=request.getHeader("Authorization");
        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");
        if(StringUtils.isBlank(token))
        {
            Result result=Result.fail(ErrorCode.NO_LOGIN.getCode(), ErrorCode.NO_LOGIN.getMsg());
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return  false;
        }
        SysUser sysUser = loginService.checkToken(token);
        if (sysUser == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        //登录成功 放行
        UserThreadLocal.put(sysUser);
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.remove();
    }
}
