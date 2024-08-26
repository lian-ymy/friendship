package com.example.friendship.constant;

/**
 * 用户中心常量集
 */
public interface UserConstant {
    /**
     * 设置用户登录态
     */
    String USER_LOGIN_STATE =  "userLogin";

    // ----------权限
    /**
     * 普通用户
     */
    Integer DEFAULT_USER = 0;

    /**
     * 管理员用户
     */
    Integer ADMIN_USER = 1;

    /**
     * 用户操作成功
     */
    Integer SUCCESS_CODE = 1;

    /**
     * redis的key值前缀
     */
    String REDIS_PREFIX = "lian:user:recommend:";

    /**
     * 实现分布式锁的key前缀
     */
    String REDIS_LOCK = "lian:redisson:recommend:";
}
