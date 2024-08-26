-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id  唯一标识'
        primary key,
    username     varchar(256)                        null comment '用户名称',
    userAccount  varchar(256)                        null comment '账号',
    avatarUrl    varchar(1024)                       null comment '用户头像',
    gender       tinyint                             null comment '性别',
    profile      varchar(512)                        null comment '用户个人简介',
    phone        varchar(128)                        null comment '电话号码',
    email        varchar(512)                        null comment '邮箱',
    userStatus   int       default 0                 null comment '状态',
    userPassword varchar(512)                        null comment '登录密码',
    updateTime   timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint   default 0                 null comment '是否删除',
    userRole     int       default 0                 null comment '用户身份 0 普通用户 1 管理员用户',
    planetCode   varchar(256)                        null comment '星球编号:用于校验登录注册用户是否合法',
    tags         varchar(1024)                       null comment '标签',
    createTime   datetime  default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '用户表';