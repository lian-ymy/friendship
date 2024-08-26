package com.example.friendship.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.friendship.common.BaseResponse;
import com.example.friendship.common.DeleteRequest;
import com.example.friendship.common.ErrorCode;
import com.example.friendship.common.ResultUtils;
import com.example.friendship.exception.BussinessException;
import com.example.friendship.mapper.TeamMapper;
import com.example.friendship.model.Team;
import com.example.friendship.model.User;
import com.example.friendship.model.dto.TeamQuery;
import com.example.friendship.model.request.TeamAddRequest;
import com.example.friendship.model.request.TeamJoinRequest;
import com.example.friendship.model.request.TeamQuitRequest;
import com.example.friendship.model.request.TeamUpdateRequest;
import com.example.friendship.model.vo.TeamUserVO;
import com.example.friendship.service.TeamService;
import com.example.friendship.service.UserService;
import com.example.friendship.service.UserTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户接口
 */
@RestController
@RequestMapping(value = "/team")
//解决跨域问题
@Slf4j
@Api(tags = "02.组队功能模块")
public class TeamController {

    @Mapper
    TeamMapper teamMapper;

    //这里需要调用业务逻辑，导入相关的变量属性
    @Resource
    TeamService teamService;

    @Resource
    UserService userService;

    @Resource
    UserTeamService userTeamService;

    @PostMapping(value = "/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if(teamAddRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team,teamAddRequest);
        } catch (Exception e) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"系统发生错误");
        }
        User loginUser = userService.getLoginUser(request);
        //这里mybatis-plus会有回写机制，当成功向数据库中写入数据后，会将分配的id值重新写回team对象
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping(value = "/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        Long teamId = deleteRequest.getId();
        if(teamId == null || teamId <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(teamId, loginUser);
        if(!result) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败！");
        }
        return ResultUtils.success(true);
    }

    @PostMapping(value = "/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if(teamUpdateRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if(!result) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"更改队伍信息失败！");
        }
        return ResultUtils.success(result);
    }

    @PostMapping(value = "/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if(teamJoinRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        if(!result) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"加入队伍失败！");
        }
        return ResultUtils.success(result);
    }

    @PostMapping(value = "quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if(teamQuitRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        if(!result) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"退出队伍失败！");
        }
        return ResultUtils.success(result);
    }

    @GetMapping(value = "/get")
    public BaseResponse<Team> getTeamById(Long teamId) {
        if(teamId == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(teamId);
        if(team == null) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(team);
    }

    @ApiOperation(value = "搜索用户")
    @GetMapping(value = "/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVO> teamUserVOS = teamService.listTeam(teamQuery, loginUser);
        return ResultUtils.success(teamUserVOS);
    }

    /**
     * 获取我创建的队伍
     * @param request
     * @return
     */
    @GetMapping(value = "/list/my/create")
    public BaseResponse<List<TeamUserVO>> getCreateTeams(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVO> teamUserVOS = teamService.getCreateTeams(loginUser);
        if(CollectionUtils.isEmpty(teamUserVOS)) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "您并未创建过队伍！");
        }
        return ResultUtils.success(teamUserVOS);
    }

    /**
     * 获取我加入的队伍
     * @param request
     * @return
     */
    @GetMapping(value = "/list/my/join")
    public BaseResponse<List<TeamUserVO>> getJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVO> teamUserVOS = teamService.getJoinTeams(teamQuery,loginUser);
        if(CollectionUtils.isEmpty(teamUserVOS)) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "您还未加入过任何队伍！");
        }
        return ResultUtils.success(teamUserVOS);
    }

    @ApiOperation(value = "按页搜索队伍")
    @GetMapping(value = "/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamQuery);
        } catch (Exception e) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(teamPage);
    }

}
