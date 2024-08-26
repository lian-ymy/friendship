package com.example.friendship.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.friendship.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author lian
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-07-10 10:46:08
*/
public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User originUser);

    Integer userLogout(HttpServletRequest request);

    List<User> searchUsersByTags(List<String> tagList);

    int editUserInfo(User user,User loginUser);

    boolean isAdmin(User user);

    User getLoginUser(HttpServletRequest request);

    Page<User> recommendUsers(long pageSize, long pageNumber, HttpServletRequest request);


    List<User> matchUsers(long num, User loginUser);
}
