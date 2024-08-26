package com.example.friendship.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.friendship.common.BaseResponse;
import com.example.friendship.common.ErrorCode;
import com.example.friendship.common.ResultUtils;
import com.example.friendship.exception.BussinessException;
import com.example.friendship.mapper.UserMapper;
import com.example.friendship.model.User;
import com.example.friendship.model.request.UserLoginRequest;
import com.example.friendship.model.request.UserRegisterRequest;
import com.example.friendship.service.UserService;
import io.swagger.annotations.Api;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.friendship.constant.UserConstant.ADMIN_USER;
import static com.example.friendship.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping(value = "/user")
//解决跨域问题
@Slf4j
@Api(tags = "01.用户管理模块")
public class UserController {

    @Mapper
    UserMapper userMapper;

    //这里需要调用业务逻辑，导入相关的变量属性
    @Resource
    UserService userService;

    @Resource
    RedisTemplate redisTemplate;

    //这里加上@RequestBody注解使得方法中的参数名能够和前端传来的参数名对应上
    @PostMapping(value = "/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "请求参数为空！");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, password, checkPassword, planetCode)) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "请求参数为空！");
        }
        long registerId = userService.userRegister(userAccount, password, checkPassword, planetCode);
        return ResultUtils.success(registerId);
    }

    @PostMapping(value = "/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BussinessException(ErrorCode.NOT_LOGIN, "用户未登录！");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "登录用户字段为空！");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        if (user == null) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "登录用户失败！");
        }
        return ResultUtils.success(user);
    }

    @PostMapping(value = "/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "请求参数为空！");
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @PostMapping(value = "/update")
    public BaseResponse<Integer> editUserInfo(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User userObj = userService.getLoginUser(request);
        int result = userService.editUserInfo(user, userObj);
        return ResultUtils.success(result);
    }

    @PostMapping(value = "/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BussinessException(ErrorCode.NO_AUTHOR, "无管理员权限！");
        }
        if (id <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "请求参数错误！");
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    @GetMapping(value = "/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    //设置一个返回当前注册登录用户的方法
    @GetMapping(value = "/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BussinessException(ErrorCode.NOT_LOGIN, "当前查询用户为空！");
        }
        //由于用户信息可能在网页中发生了变化，而从网址中获取的对象可能还是之前的对象，因此建议从数据库中查询返回
        Long id = currentUser.getId();

        User user = userService.getById(id);
        //返回脱敏后的用户信息
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 根据指定用户名查询指定用户
     * 由于这里是查询用户，所以应当为get请求
     *
     * @param username 要查询的用户名
     * @return 查询到的用户
     */
    @GetMapping(value = "/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BussinessException(ErrorCode.NO_AUTHOR, "无管理员权限！");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList
                .stream()
                .map(user -> userService.getSafetyUser(user))
                .collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping(value = "/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNumber, HttpServletRequest request) {
        Page<User> userPage = userService.recommendUsers(pageSize, pageNumber, request);
        if(userPage == null) {
            return ResultUtils.success(new Page<>());
        }
        return ResultUtils.success(userPage);
    }

    @GetMapping(value = "/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if(num <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<User> userList = userService.matchUsers(num, loginUser);
        return ResultUtils.success(userList);
    }

    /**
     * 判断是否为管理员
     *
     * @param request 从前段接受的请求
     * @return 返回是否为管理员的判断情况
     */
    private boolean isAdmin(HttpServletRequest request) {
        //一定要鉴别权限，只有管理员能够对数据进行查询删除
        //访问静态成员必须通过类名进行访问，不能使用实际构造的类对象进行访问
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        //这里对应的对象可能为空，导致后面报错空指针异常，需要提前判断一下
        return user != null && Objects.equals(user.getUserRole(), ADMIN_USER);
    }
}
