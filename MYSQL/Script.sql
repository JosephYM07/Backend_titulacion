-- auto-generated definition
create table user
(
    id            int auto_increment
        primary key,
    username      varchar(255)                                           not null,
    business_name varchar(255)                                           null,
    password      varchar(255)                                           not null,
    email         varchar(255)                                           null,
    phone_number  varchar(255)                                           null,
    role          enum ('ADMIN', 'PERSONAL_CENTRO_DE_SERVICIOS', 'USER') null,
    permiso       bit                                                    not null,
    constraint UK_sb8bbouer5wak8vyiiy4pf2bx
        unique (username)
);

create table password_reset_tokens
(
    id          bigint auto_increment
        primary key,
    token       varchar(255) not null,
    expiry_date datetime(6)  not null,
    user_id     int          not null,
    constraint UK_71lqwbwtklmljk3qlsugr1mig
        unique (token),
    constraint FK20xweju6fxkxcx3taa9elhtew
        foreign key (user_id) references user (id)
);

create table refresh_tokens
(
    id          bigint auto_increment
        primary key,
    token varchar(255) not null,
    expiry_date datetime(6)  not null,
    user_id     int          not null,
    constraint UK_ghpmfn23vmxfu3spu3lfg4r2d
        unique (token),
    constraint FKjwc9veyjcjfkej6rnnbsijfvh
        foreign key (user_id) references user (id)
);

UPDATE user
SET permiso = b'1'
WHERE username = 'NissanUIO';
