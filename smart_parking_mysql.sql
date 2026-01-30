create database if not exists smart_parking
    default character set utf8mb4
    default collate utf8mb4_0900_ai_ci;

use smart_parking;

create table if not exists sys_user
(
    id            bigint primary key auto_increment,
    username      varchar(64)  not null unique,
    password_hash varchar(128) not null,
    role          varchar(16)  not null,
    phone         varchar(128) null
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;

create table if not exists parking_space
(
    id     bigint primary key auto_increment,
    code   varchar(64)  not null unique,
    area   varchar(64)  not null,
    status varchar(16)  not null,
    type   varchar(32)  not null,
    key idx_parking_space_status_id (status, id)
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;

create table if not exists parking_order
(
    id               bigint primary key auto_increment,
    user_id          bigint        not null,
    parking_space_id bigint        not null,
    entry_time       datetime      null,
    exit_time        datetime      null,
    estimated_fee    decimal(18,2) null,
    paid_fee         decimal(18,2) null,
    status           varchar(16)   not null,
    key idx_parking_order_user (user_id, id),
    key idx_parking_order_space (parking_space_id, id),
    constraint fk_parking_order_user_id foreign key (user_id) references sys_user (id),
    constraint fk_parking_order_parking_space_id foreign key (parking_space_id) references parking_space (id)
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;

create table if not exists fee_rule
(
    id                  bigint primary key auto_increment,
    base_price_per_hour decimal(18,2) not null,
    cap_price           decimal(18,2) not null,
    free_minutes        int           not null
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;

create table if not exists user_wallet
(
    id      bigint primary key auto_increment,
    user_id bigint        not null unique,
    balance decimal(18,2) not null,
    version bigint        not null default 0,
    constraint fk_user_wallet_user_id foreign key (user_id) references sys_user (id) on delete cascade
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;

create table if not exists wallet_log
(
    id         bigint primary key auto_increment,
    wallet_id  bigint        not null,
    amount     decimal(18,2) not null,
    type       varchar(32)   not null,
    order_id   bigint        null,
    created_at datetime      not null default current_timestamp,
    key idx_wallet_log_wallet (wallet_id, id),
    key idx_wallet_log_order (order_id, id),
    constraint fk_wallet_log_wallet_id foreign key (wallet_id) references user_wallet (id),
    constraint fk_wallet_log_order_id foreign key (order_id) references parking_order (id) on delete set null
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;

create table if not exists audit_log
(
    id            bigint primary key auto_increment,
    actor_user_id bigint       null,
    actor_role    varchar(16)  null,
    action        varchar(64)  not null,
    resource_type varchar(64)  not null,
    resource_id   varchar(64)  null,
    detail_json   text         null,
    created_at    datetime     not null default current_timestamp,
    key idx_audit_log_created_at (created_at),
    key idx_audit_log_actor_created_at (actor_user_id, created_at),
    constraint fk_audit_log_actor_user_id foreign key (actor_user_id) references sys_user (id) on delete set null
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;

insert into fee_rule(id, base_price_per_hour, cap_price, free_minutes)
values (1, 10.00, 100.00, 0)
on duplicate key update base_price_per_hour = values(base_price_per_hour),
                        cap_price = values(cap_price),
                        free_minutes = values(free_minutes);
