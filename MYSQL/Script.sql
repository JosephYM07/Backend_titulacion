-- Crear tabla 'user'
CREATE TABLE user
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(255)                                           NOT NULL UNIQUE,
    business_name VARCHAR(255)                                           NULL,
    password      VARCHAR(255)                                           NOT NULL,
    email         VARCHAR(255)                                           NULL,
    phone_number  VARCHAR(255)                                           NULL,
    role          ENUM ('ADMIN', 'PERSONAL_CENTRO_DE_SERVICIOS', 'USER') NULL,
    permiso       BIT                                                    NOT NULL
);
-- Crear tabla 'password_reset_tokens' con ON DELETE CASCADE
CREATE TABLE password_reset_tokens
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME(6)  NOT NULL,
    user_id     INT          NOT NULL,
    CONSTRAINT FK_password_reset_tokens_user FOREIGN KEY (user_id)
        REFERENCES user (id)
        ON DELETE CASCADE
);

-- Crear tabla 'refresh_tokens' con ON DELETE CASCADE
CREATE TABLE refresh_tokens
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME(6)  NOT NULL,
    user_id     INT          NOT NULL,
    CONSTRAINT FK_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES user (id)
        ON DELETE CASCADE
);