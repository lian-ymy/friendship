-- auto-generated definition
create table tag
(
    id         bigint auto_increment comment 'id  唯一标识'
        primary key,
    tagName    varchar(256)                        null comment '标签名称',
    userId     bigint                              null comment '用户 id',
    parentId   bigint                              null comment '父标签 id',
    isParent   tinyint                             null comment '1-父标签 0-不是父标签',
    createTime datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint   default 0                 null comment '是否删除',
    constraint tagName
        unique (tagName)
)
    comment '标签';

create index userId_index
    on tag (userId);

