package com.example.friendship.service.iml;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.friendship.mapper.TagMapper;
import com.example.friendship.model.Tag;
import com.example.friendship.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
* @author lian
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-07-10 10:36:16
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

    @Resource
    TagMapper tagMapper;
}




