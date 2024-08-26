package com.example.friendship.service.iml;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.friendship.common.ErrorCode;
import com.example.friendship.exception.BussinessException;
import com.example.friendship.mapper.UserMapper;
import com.example.friendship.model.User;
import com.example.friendship.service.UserService;
import com.example.friendship.util.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.friendship.constant.UserConstant.*;

/**
* @author lian
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-07-10 10:46:08
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    //对数据库的操作可以通过接口实现类进行实现，也可以通过mapper中的方法实现
    @Resource
    UserMapper userMapper;

    /**
     * 盐值、混淆密码
     */
    private static final String SALT = "ymy";

    @Resource
    RedisTemplate redisTemplate;

    /**
     * 用户注册
     *
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 返回注册的id号
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "请求参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户名长度小于三位！");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "密码长度小于八位");
        }
        if (planetCode.length() > 5) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "星球编号不符合要求！");
        }
        //判断账户是否包含特殊字符，如果包含特殊字符就抛出异常
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(userAccount);
        if (matcher.find()) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户账户包含特殊字符！！");
        }
        //密码与校验密码相同
        //字符串比较使用equals进行比较
        if (!userPassword.equals(checkPassword)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "输入密码与校验密码不同！！");
        }
        //账户不能重复，这一步是对数据库进行查询，如果说条件不满足就直接返回，防止有后续的冗余操作
        Long count = query().eq("userAccount", userAccount).count();
        if (count > 0) {
            throw new BussinessException(ErrorCode.REPEATED_USER, "用户账号重复！！");
        }
        //星球编号不能重复
        count = query().eq("planetCode", planetCode).count();
        if (count > 0) {
            throw new BussinessException(ErrorCode.REPEATED_USER, "星球编号重复！！");
        }
        //加密，将密码经过MD5单向处理之后保存到数据库中
        //将真实密码与盐值共同进行加密，起到混淆视听的作用
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //向数据库中插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean save = save(user);
        if (!save) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "数据库插入数据失败！！");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1、校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "请求参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户名长度小于三位！");
        }
        if (userPassword.length() < 8) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "密码长度小于八位");
        }
        //判断账户是否包含特殊字符，如果包含特殊字符就抛出异常
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(userAccount);
        if (matcher.find()) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户账户包含特殊字符！！");
        }
        //2、查询用户信息
        //对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询数据库中是否有对应登录用户的信息
        List<User> list = query().eq("userAccount", userAccount).eq("userPassword", encryptPassword).select().list();
        if (list == null || list.isEmpty()) {
            //如果这里发现了错误，使用日志打印错误信息，最好使用英文，方便其他应用对信息进行检索
            log.info("User login failed, userAccount can not match password");
            throw new BussinessException(ErrorCode.NULL_ERROR, "查询不到对应用户信息！！");
        }

        //3、用户脱敏，将全部的用户信息经过脱敏后返回给前端展示到页面上
        User user = this.getSafetyUser(list.get(0));

        //4、记录用户登录态
        //这里获取对应会话，通过session设置属性值传递给前端进行判断
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        //返回脱敏后的用户信息，用于在网页中展示出来
        return user;
    }


    /**
     * 用户脱敏
     *
     * @param originUser 一开始的原始用户数据
     * @return 脱敏处理后的用户数据
     */
    @Override
    public User getSafetyUser(User originUser) {
        //每次一个函数中新接收了一个参数之后都要判断它是否合法，如果不合法要立即返回
        if(originUser == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "输入参数为空！");
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(0);
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    //我们设置用户登录时是通过给用户登录态一个特定的键值对完成的，这里我们只需要移除这个用户登录态的键值对即可
    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return SUCCESS_CODE;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagList) {
        if(CollectionUtils.isEmpty(tagList)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
//        第一种查询方式：直接使用sql查询
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (String tagName : tagList) {
            userQueryWrapper = userQueryWrapper.like("tags",tagName);
        }
        List<User> users = userMapper.selectList(userQueryWrapper);
        return users.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int editUserInfo(User user, User loginUser) {
        Long userId = user.getId();
        if(userId <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //是管理员时，可以对任意信息进行修改
        //不是管理员时，只可以对自己的信息进行修改
        //todo 补充校验，如果用户没有传递任何要更新的值，就直接报错，不用执行update语句
        if(!isAdmin(loginUser) && !Objects.equals(user.getId(), loginUser.getId())) {
            throw new BussinessException(ErrorCode.NO_AUTHOR);
        }
        User olderUser = userMapper.selectById(userId);
        if(olderUser == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Deprecated
    public List<User> searchUsersByTagsInMemory(List<String> tagList) {
        //第二种查询方式：直接在内存中查询
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(userQueryWrapper);
        Gson gson = new Gson();
        //使用filter过滤函数，如果说这个对象全部包含当前要查询的这个标签，就保留下来，否则就剔除它
//        return users.parallelStream().filter()  并发线程池处理数据，将搜索数据的工作分成多个线程并发同时进行
        return users.stream().filter(user -> {
            String tags = user.getTags();
            //所有从数据库中取出的值都要进行判空校验
            if(tags.isBlank()) {
                return false;
            }
            //使用gson库将json类型的字符串转换为一个具体的对象
            Set<String> tagNameSet = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            //判断是否为空，如果为空就通过这个函数给它设置一个默认值
            tagNameSet = Optional.ofNullable(tagNameSet).orElse(new HashSet<>());
            for (String tagName : tagList) {
                if (!tagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public boolean isAdmin(User user) {
        //一定要鉴别权限，只有管理员能够对数据进行查询删除
        //这里对应的对象可能为空，导致后面报错空指针异常，需要提前判断一下
        return user != null && Objects.equals(user.getUserRole(), ADMIN_USER);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null) {
            return null;
        }
        return (User) userObj;
    }

    @Override
    public Page<User> recommendUsers(long pageSize, long pageNumber, HttpServletRequest request) {
        //设置当前存储key值
        User loginUser = this.getLoginUser(request);
        String key = REDIS_PREFIX + loginUser.getId();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User>userList = (Page<User>) valueOperations.get(key);
        if(userList!=null) {
            //如果数据不为空，说明缓存中存在数据，则直接返回
            return userList;
        }
        //给用户展示相似的用户进行推荐，这里不需要管理员权限也可以进行展示推荐
        //由于用户量很多，所以这里要对查询到的用户进行分页，规定一页展示多少用户数据
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userList = this.page(new Page<>(pageNumber, pageSize), queryWrapper);
        //反之，查询出来数据之后就将数据再添加进缓存中
        try {
            valueOperations.set(key, userList, 1440, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.info("redis error" + e);
        }
        return userList;
    }

    /**
     * 根据标签匹配与当前登录用户爱好最相近的用户
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        String loginUserTags = loginUser.getTags();
        Long loginUserId = loginUser.getId();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(loginUserTags, new TypeToken<List<String>>() {
        }.getType());
        //查找与当前用户标签列表相似最多的用户集合，由于要从所有用户列表中进行查询，所以可以通过几个步骤来
        //加快最后查询的速度
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //由于只需要查找标签列表与用户id，所以可以通过只查询这两列减少其他查询的损耗时间
        userQueryWrapper.select("id","tags");
        userQueryWrapper.isNotNull("tags");
        List<User> userList = this.list(userQueryWrapper);
        //通过向list链表中存储pair数据，pair中以用户为键，以对应的修改次数为值，方便进行排序与查找
        List<Pair<Integer, User>> userScoreList = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            //如果这个用户的标签列表为空或者查找到了自己，就直接跳过
            if(StringUtils.isBlank(userTags) || user.getId().equals(loginUserId)) {
                continue;
            }
            List<String> userTagList =
                    gson.fromJson(userTags, new TypeToken<List<String>>() {}.getType());
            int modify = AlgorithmUtils.minDistance(tagList,userTagList);
            userScoreList.add(new Pair<>(modify,user));
        }
        List<Pair<Integer, User>> topNumUserList =
                userScoreList.stream()
                        .sorted((a, b) -> a.getKey() - b.getKey())
                        .limit(num)
                        .collect(Collectors.toList());
        //原本顺序的idList
        List<Long> idList = topNumUserList.stream()
                .map(pair -> pair.getValue().getId())
                .collect(Collectors.toList());
        //这里使用in之后会打乱原本已经排序好的推荐用户列表顺序
        userQueryWrapper.clear();
        userQueryWrapper.in("id",idList);
        Map<Long, List<User>> listMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUsers = new ArrayList<>();
        for (Long id : idList) {
            finalUsers.add(listMap.get(id).get(0));
        }
        return finalUsers;
    }
}




