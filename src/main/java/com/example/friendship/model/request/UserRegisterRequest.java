package com.example.friendship.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户请求体
 */
@Data
public class UserRegisterRequest implements Serializable {
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    //注册时根据星球编号进行校验，添加星球编号这一项
    private String planetCode;
}