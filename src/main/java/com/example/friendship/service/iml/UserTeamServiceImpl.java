package com.example.friendship.service.iml;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.friendship.model.UserTeam;
import com.example.friendship.service.UserTeamService;
import com.example.friendship.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author lian
* @description 针对表【user_team(用户--队伍关系表)】的数据库操作Service实现
* @createDate 2024-07-20 10:50:29
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




