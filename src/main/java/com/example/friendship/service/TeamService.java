package com.example.friendship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.friendship.model.Team;
import com.example.friendship.model.User;
import com.example.friendship.model.dto.TeamQuery;
import com.example.friendship.model.request.TeamJoinRequest;
import com.example.friendship.model.request.TeamQuitRequest;
import com.example.friendship.model.request.TeamUpdateRequest;
import com.example.friendship.model.vo.TeamUserVO;

import java.util.List;

/**
* @author lian
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-07-20 10:50:20
*/
public interface TeamService extends IService<Team> {
    long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeam(TeamQuery teamQuery, User loginUser);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(Long teamId, User loginUser);

    List<TeamUserVO> getCreateTeams(User loginUser);

    List<TeamUserVO> getJoinTeams(TeamQuery teamQuery, User loginUser);
}
