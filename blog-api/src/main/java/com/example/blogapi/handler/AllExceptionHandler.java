package com.example.blogapi.handler;

import com.example.blogapi.vo.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice   //给所有control用aop切面加入了异常处理
public class AllExceptionHandler {
    //处理Exception的异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result doException(Exception ex)
    {
        ex.printStackTrace();
        return Result.fail(-999,"系统异常");
    }
}
