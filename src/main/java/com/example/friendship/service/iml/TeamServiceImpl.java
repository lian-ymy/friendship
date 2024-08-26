package com.example.friendship.service.iml;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.friendship.common.ErrorCode;
import com.example.friendship.constant.TeamStatusEnum;
import com.example.friendship.exception.BussinessException;
import com.example.friendship.mapper.TeamMapper;
import com.example.friendship.model.Team;
import com.example.friendship.model.User;
import com.example.friendship.model.UserTeam;
import com.example.friendship.model.dto.TeamQuery;
import com.example.friendship.model.request.TeamJoinRequest;
import com.example.friendship.model.request.TeamQuitRequest;
import com.example.friendship.model.request.TeamUpdateRequest;
import com.example.friendship.model.vo.TeamUserVO;
import com.example.friendship.model.vo.UserVO;
import com.example.friendship.service.TeamService;
import com.example.friendship.service.UserService;
import com.example.friendship.service.UserTeamService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.friendship.constant.UserConstant.ADMIN_USER;

/**
 * @author lian
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2024-07-20 10:50:20
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    UserTeamService userTeamService;

    @Resource
    UserService userService;

    @Override
    @Transactional
    public long addTeam(Team team, User loginUser) {
        //1、请求参数是否为空，这里之所以没有再对登录用户做校验是因为在外层已经将用户再校验了一遍
        if (team == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "传递参数为空！");
        }
        //2、是否登录，未登录不允许创建队伍
        if (loginUser == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "传递参数为空！");
        }
        //3、校验信息
        //a.队伍人数 >1 且 <=20
        long maxNum = team.getMaxNum();
        if (maxNum <= 1 || maxNum > 20) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍人数错误！");
        }
        //b.队伍名字小于20
        String name = team.getTeamName();
        if (name == null || name.length() > 20) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍名字过长！");
        }
        //c.队伍描述<512
        String description = team.getTeamDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍描述信息过长！");
        }
        //d.status是否为公开，不传默认为0
        //e.如果status为加密状态，一定要有密码，且密码<=32
        TeamStatusEnum statusNumber = TeamStatusEnum.getStatusNumber(team.getTeamStatus());
        if (statusNumber.equals(TeamStatusEnum.SECRET)) {
            String password = team.getTeamPassword();
            if (password == null || password.length() > 32) {
                throw new BussinessException(ErrorCode.PARAMS_ERROR, "输入密码有误！");
            }
        }
        //f.超时时间>当前时间
        if (new Date().after(team.getExpireTime())) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍时间已过期！");
        }
        //g.校验用户最多创建5个队伍
        // todo 这里必须进行判断，如果用户再前端点了一百下创建队伍，有可能创建了一百支队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", team.getUserId());
        long count = this.count(queryWrapper);
        if (count > 5) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "创建队伍数量达到上限！");
        }
        //这两个插入操作一定要确保同时进行，这里应用事务进行操作
        //4、插入队伍信息到队伍表
        //5、同时插入用户到队伍表与用户队伍关系表中
        Long teamId = insertTeam(team, loginUser);
        if (teamId == null) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "插入队伍数据失败");
        }
        return teamId;
    }

    /**
     * 列出符合要求的所有队伍，通过扩展接口，使得方法能够更多地被复用
     * @param teamQuery
     * @param loginUser
     * @return
     */
    @Override
    public List<TeamUserVO> listTeam(TeamQuery teamQuery, User loginUser) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //如果传递的的teamQuery对象为空，表示查询所有队伍用户数据
        if (teamQuery != null) {
            //添加判断队伍名字参数
            String teamName = teamQuery.getTeamName();
            if (teamName != null) {
                queryWrapper.like("teamName", teamName);
            }
            //添加可以匹配队伍名称与队伍描述的公共字段
            String searchText = teamQuery.getSearchText();
            if(searchText!=null) {
                queryWrapper.and(qw -> qw.like("teamName",searchText).or().like("teamDescription",searchText));
            }
            //添加判断队伍描述
            String description = teamQuery.getTeamDescription();
            if (description != null) {
                queryWrapper.like("teamDescription", description);
            }
            //添加判断队伍编号
            Long teamId = teamQuery.getId();
            if (teamId != null) {
                queryWrapper.eq("id", teamId);
            }
            //添加队伍编号在队伍id集合中判断
            List<Long> teamIdList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(teamIdList)) {
                queryWrapper.in("id",teamIdList);
            }
            //添加判断队伍容纳最大数量
            Long maxNum = teamQuery.getMaxNum();
            if (maxNum != null) {
                queryWrapper.eq("maxNum", maxNum);
            }
            //添加判断队伍状态，只有管理员可以查看所有的房间
            boolean isAdmin = userService.isAdmin(loginUser);
            Integer teamStatus = teamQuery.getTeamStatus();
            TeamStatusEnum statusNumber;
            if(teamStatus == null) {
                statusNumber = TeamStatusEnum.PUBLIC;
            }else {
                statusNumber = TeamStatusEnum.getStatusNumber(teamStatus);
                if (statusNumber == null) {
                    statusNumber = TeamStatusEnum.PUBLIC;
                }
            }
            if(!isAdmin && statusNumber.equals(TeamStatusEnum.PRIVATE)) {
                if(!teamQuery.getUserId().equals(loginUser.getId())) {
                    throw new BussinessException(ErrorCode.NO_AUTHOR, "无管理员权限！");
                }
            }
            if (teamStatus != null) {
                queryWrapper.eq("teamStatus", teamStatus);
            }
            //添加判断创建队伍的用户id
            Long userId = teamQuery.getUserId();
            if (userId != null) {
                queryWrapper.eq("userId", userId);
            }
        }
        //当前时间必须小于队伍的过期时间才展示出来
        //expireTime = null or expireTime > new Date()
        queryWrapper.and(qw -> qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        //1、查询符合条件的所有队伍集合
        List<Team> teamList = this.list(queryWrapper);
        //判断查询得到的队伍集合是否为空
        if (CollectionUtils.isEmpty(teamList)) {
            //如果为空，直接返回空集合即可
            return new ArrayList<>();
        }
        //将所有的用户信息以及对应的队伍信息打包到前端队伍用户展示类中
        List<TeamUserVO> teamUserVOS = new ArrayList<>();
        ///2根据得到的所有队伍信息查询所有对应的创建者用户信息
        for (Team team : teamList) {
            User createUser = userService.getById(team.getUserId());
            TeamUserVO teamUserVO = new TeamUserVO();
            UserVO userVO = new UserVO();
            try {
                if(createUser != null) {
                    BeanUtils.copyProperties(userVO,createUser);
                    teamUserVO.setCreateUser(userVO);
                }
                BeanUtils.copyProperties(teamUserVO, team);
            } catch (Exception e) {
                throw new BussinessException(ErrorCode.SYSTEM_ERROR, "系统内部运行错误！");
            }
            teamUserVOS.add(teamUserVO);
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        List<Long> idList = teamUserVOS.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        //给查询到的视图添加当前登录用户是否在队伍中字段
        userTeamQueryWrapper.eq("userId",loginUser.getId());
        userTeamQueryWrapper.in("teamId",idList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        //3、将当前登录用户已经加入队伍的队伍id添加到set集合中方便进行判断
        Set<Long> teamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        teamUserVOS.forEach(teamUserVO -> {
            boolean contains = teamIdSet.contains(teamUserVO.getId());
            teamUserVO.setHasJoin(contains);
        });
        userTeamQueryWrapper.clear();
        userTeamQueryWrapper.in("teamId",idList);
        List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
        Map<Long, List<UserTeam>> idUserTeamMap = userTeams.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamUserVOS.forEach(teamUserVO ->
                teamUserVO.setJoinNum(idUserTeamMap.getOrDefault(teamUserVO.getId(), new ArrayList<>()).size()));
        return teamUserVOS;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamUpdateRequest.getId();
        if(teamId == null || teamId <= 0)  {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(teamId);
        if(oldTeam == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR,"队伍不存在！");
        }
        //只有管理员或者创建者可以修改队伍信息
        if(!userService.isAdmin(loginUser) && !teamUpdateRequest.getUserId().equals(loginUser.getId())) {
            throw new BussinessException(ErrorCode.NO_AUTHOR);
        }
        Integer teamStatus = teamUpdateRequest.getTeamStatus();
        TeamStatusEnum statusNumber = TeamStatusEnum.getStatusNumber(teamStatus);
        if(statusNumber.equals(TeamStatusEnum.SECRET)) {
            if(StringUtils.isBlank(teamUpdateRequest.getTeamPassword())) {
                throw new BussinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码！");
            }
        }
        //修改队伍信息
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team,teamUpdateRequest);
        } catch (Exception e) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR);
        }
        boolean result = this.updateById(team);
        return result;
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if(teamJoinRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getId();
        if(teamId == null || teamId <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR,"队伍不存在!");
        }
        Date expireTime = team.getExpireTime();
        if(expireTime != null && expireTime.before(new Date())) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"队伍已过期!");
        }
        if(team.getUserId().equals(loginUser.getId())) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"创建者已在队伍中!");
        }
        Integer teamStatus = team.getTeamStatus();
        TeamStatusEnum statusNumber = TeamStatusEnum.getStatusNumber(teamStatus);
        if(TeamStatusEnum.PRIVATE.equals(statusNumber)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"不能加入私有的队伍！");
        }
        String teamPassword = teamJoinRequest.getTeamPassword();
        if(TeamStatusEnum.SECRET.equals(statusNumber)) {
            if(teamPassword == null || !team.getTeamPassword().equals(teamPassword)) {
                throw new BussinessException(ErrorCode.PARAMS_ERROR,"输入密码错误！");
            }
        }
        //判断用户加入队伍数量是否达到上限
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        Long userId = loginUser.getId();
        queryWrapper.eq("userId",userId);
        long count = userTeamService.count(queryWrapper);
        if(count >= 5) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户加入队伍数量已经到达上限！");
        }
        //判断用户是否已经加入当前队 伍
        queryWrapper.eq("teamId",teamId);
        count = userTeamService.count(queryWrapper);
        if(count > 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户已经加入当前队伍！");
        }
        //判断当前要加入的队伍是否已经到达人数上限
        queryWrapper.clear();
        queryWrapper.eq("teamId", team);
        count = userTeamService.count(queryWrapper);
        if(count >= team.getMaxNum()) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍人数已经到达上限！");
        }
        //新增队伍用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long teamId = teamQuitRequest.getId();
        if(teamId == 0 || teamId <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"队伍id不满足条件！");
        }
        Team team = this.getById(teamId);
        if(team == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        userTeamQueryWrapper.eq("userId",userId);
        long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
        if(hasUserJoinTeam == 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户未加入队伍");
        }
        userTeamQueryWrapper.clear();
        userTeamQueryWrapper.eq("teamId",teamId);
        long count = userTeamService.count(userTeamQueryWrapper);
        //如果队伍只有一个人，就解散队伍
        if(count == 1) {
            this.removeById(teamId);
        } else {
            //如果是队长，就将权限转移给第二个加入队伍的人
            if(team.getUserId().equals(userId)) {
                //把队伍转交给第二早加入队伍的成员
                //成员id越小说明加入的越早
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BussinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUser = userTeamList.get(1);
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUser.getUserId());
                boolean result = this.updateById(updateTeam);
                if(!result) {
                    throw new BussinessException(ErrorCode.SYSTEM_ERROR,"更新队伍信息失败！");
                }
            }
        }
        userTeamQueryWrapper.eq("userId",userId);
        return userTeamService.remove(userTeamQueryWrapper);
    }

    @Override
    public boolean deleteTeam(Long teamId, User loginUser) {
        if(teamId == null || teamId <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"输入参数错误！");
        }
        Team team = this.getById(teamId);
        if(team == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        Long userId = loginUser.getId();
        if(!team.getUserId().equals(userId)) {
            throw new BussinessException(ErrorCode.NO_AUTHOR,"无权限！");
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR);
        }
        return this.removeById(teamId);
    }

    @Override
    public List<TeamUserVO> getCreateTeams(User loginUser) {
        if(loginUser == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户数据为空");
        }
        Long userId = loginUser.getId();
        if(userId == null || userId <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)) {
            throw new BussinessException(ErrorCode.NULL_ERROR,"您还未创建过任何队伍！");
        }
        UserVO createUserVO = new UserVO();
        try {
            BeanUtils.copyProperties(createUserVO,loginUser);
        } catch (Exception e) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR);
        }
        return teamList.stream().map(team -> {
            TeamUserVO teamUserVO = new TeamUserVO();
            try {
                BeanUtils.copyProperties(teamUserVO, team);
            } catch (Exception e) {
                throw new BussinessException(ErrorCode.SYSTEM_ERROR);
            }
            teamUserVO.setCreateUser(createUserVO);
            return teamUserVO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户加入的所有队伍，由于上面实现了查询所有用户，所以这里可以通过增加参数完成复用
     * @param teamQuery
     * @param loginUser
     * @return
     */
    @Override
    public List<TeamUserVO> getJoinTeams(TeamQuery teamQuery, User loginUser) {
        if(loginUser == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户未登录");
        }
        Long userId = loginUser.getId();
        if(userId == null || userId <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //根据用户队伍的队伍id进行综合打包，按照队伍id进行打包
        Map<Long, List<UserTeam>> listMap =
                userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long>idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        loginUser.setUserStatus(ADMIN_USER);
        return this.listTeam(teamQuery, loginUser);
    }


    //如果出现异常就发生回滚，如果不设置，一般情况下只有出现RuntimeException才发生回滚
    @Transactional(rollbackFor = Exception.class)
    public Long insertTeam(Team team, User loginUser) {
        team.setId(0L);
        Long userId = loginUser.getId();
        team.setUserId(userId);
        boolean result = this.save(team);
        if (!result || team.getId() == null) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "插入队伍数据发生错误！");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(team.getId());
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);
        if (!save) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "插入用户队伍关系数据表发生错误！");
        }
        return team.getId();
    }
}




