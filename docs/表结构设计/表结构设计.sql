create table gateway_group
(
    id          bigint                             not null comment '唯一标识id'
        primary key,
    group_name  varchar(100)                       not null comment '分组名称',
    group_key   varchar(100)                       not null comment '分组唯一标识',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint gateway_group_pk
        unique (group_key)
)
    comment '网关实例分组表';

create table gateway_group_detail
(
    id             bigint                             not null comment '唯一id'
        primary key,
    group_id       bigint                             not null comment '分组唯一标识id',
    detail_name    varchar(100)                       not null comment '网关实例名称',
    detail_address varchar(100)                       not null comment '网关实例地址',
    status         int      default 0                 not null comment '网关实例启用状态',
    detail_weight  int      default 1                 not null comment '网关实例分配权重',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint group_id
        unique (group_id, detail_address)
)
    comment '网关实例信息表';

create index idx_group_id
    on gateway_group_detail (group_id);

create table gateway_interface
(
    id             bigint                             not null comment '唯一id'
        primary key,
    server_id      bigint                             not null comment '网关服务唯一id',
    interface_name varchar(100)                       null comment '接口名',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '接口信息表';

create index idx_server_id
    on gateway_interface (server_id);

create table gateway_method
(
    id             bigint                             not null comment '唯一id'
        primary key,
    interface_id   bigint                             not null comment '接口唯一标识id',
    method_name    varchar(100)                       not null comment '方法名称',
    parameter_type varchar(1000)                      not null comment '参数类型',
    url            varchar(1000)                       not null comment '方法请求路径',
    is_auth        int      default 1                 not null comment '是否鉴权',
    is_http        int                                not null comment '是否是HTTP请求',
    http_type      varchar(100)                       null comment 'HTTP请求类型',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '方法信息表';

create table gateway_server
(
    id          bigint                             not null comment '唯一id'
        primary key,
    server_name varchar(100)                       not null comment '服务名',
    status      int      default 0                 not null comment '启用状态',
    safe_key    varchar(100)                       not null comment '安全组唯一标识',
    safe_secret varchar(100)                       not null comment '安全组秘钥',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint gateway_server_pk
        unique (safe_key)
)
    comment '网关系统表';

create table gateway_server_detail
(
    id             bigint                             not null comment '唯一id'
        primary key,
    server_id      bigint                             not null comment '系统唯一标识id',
    server_address varchar(100)                       not null comment '系统实例地址',
    status         int      default 0                 not null comment '系统实例启用状态',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint gateway_server_detail_pk
        unique (server_address)
)
    comment '系统详细信息表';

create table gateway_server_group_rel
(
    id          bigint                             not null comment '唯一id'
        primary key,
    server_id   bigint                             not null comment '网关服务唯一id',
    group_id    bigint                             not null comment '网关系统分组唯一id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '网关系统和实例分组关联表';

-- 网关限流配置表
create table gateway_rate_limit
(
    id           bigint                             not null comment '唯一id'
        primary key,
    rule_name    varchar(100)                       not null comment '限流规则名称',
    limit_type   varchar(20)                        not null comment '限流类型：GLOBAL(全局)、SERVICE(服务级)、INTERFACE(接口级)、IP(IP级)',
    limit_target varchar(200)                       not null comment '限流目标：服务名、接口URL、IP地址等',
    limit_count  int                                not null comment '限流阈值（每秒请求数）',
    time_window  int      default 1                 not null comment '时间窗口（秒）',
    status       tinyint  default 1                 not null comment '是否启用：0-禁用，1-启用',
    strategy     varchar(20) default 'TOKEN_BUCKET' not null comment '限流策略：TOKEN_BUCKET(令牌桶)、SLIDING_WINDOW(滑动窗口)',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '网关限流配置表';

create index idx_limit_type
    on gateway_rate_limit (limit_type);

create index idx_limit_target
    on gateway_rate_limit (limit_target);

create index idx_status
    on gateway_rate_limit (status);

-- 插入示例限流配置数据
INSERT INTO gateway_rate_limit (id, rule_name, limit_type, limit_target, limit_count, time_window, status, strategy) VALUES
(1, '全局限流', 'GLOBAL', 'GLOBAL', 10000, 1, 1, 'TOKEN_BUCKET'),
(2, '测试服务限流', 'SERVICE', 'test-service', 1000, 1, 1, 'TOKEN_BUCKET'),
(3, '登录接口限流', 'INTERFACE', 'test-service:/api/login', 100, 1, 1, 'SLIDING_WINDOW'),
(4, 'IP限流', 'IP', '192.168.1.100', 50, 1, 1, 'TOKEN_BUCKET');
