package com.example.friendship.common;

public enum ErrorCode {
    //枚举类通过自己定义构造方法，在类中创建几个全局都需要用到的异常返回值
    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求参数为空",""),
    NOT_LOGIN(40100,"用户未登录",""),
    NO_AUTHOR(40101,"无管理员权限",""),
    REPEATED_USER(40400,"用户账户重复",""),
    SYSTEM_ERROR(50000,"运行系统内部错误","")
    ;

    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述
     */
    private final String description;


    ErrorCode(int code, String message,String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getMessage() {
        return message;
    }
}
