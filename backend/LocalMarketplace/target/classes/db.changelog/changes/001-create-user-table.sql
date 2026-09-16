--liquibase formatted sql

--changeset fmi:001-create-user-table

CREATE TABLE users
(
    username   VARCHAR(50)  NOT NULL,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    phone      VARCHAR(20),
    user_type  VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active     BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_users PRIMARY KEY (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone)
);

--rollback DROP TABLE show;