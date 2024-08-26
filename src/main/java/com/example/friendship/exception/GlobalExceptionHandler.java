package com.example.friendship.exception;

import com.example.friendship.common.BaseResponse;
import com.example.friendship.common.ErrorCode;
import com.example.friendship.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 自定义全局异常处理类
 *
 * 可以通过捕获某个异常，返回给前端希望展示的更加详细的信息，同时
 * 可以屏蔽掉有关于服务器内部的状态信息，保护项目内部安全
 *
 * 这里通常用@RestControllerAdvice与@ExceptionHandler注解进行异常捕捉与处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BussinessException.class)
    public BaseResponse BusinessExceptionHandler(BussinessException e) {
        log.info("BusinessError:" + e.getMessage(),e);
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse RuntimeErrorExceptionHandler(RuntimeException e) {
        //一旦系统运行时出现了运行错误，那么日志中就会打印下面消息并记录下来
        log.info("Runtime Error!" + e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }
}
