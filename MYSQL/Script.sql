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

CREATE TABLE user_sequence (
                               sequence_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id     INT NOT NULL,
                               created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT FK_user_sequence_user FOREIGN KEY (user_id)
                                   REFERENCES user (id)
                                   ON DELETE CASCADE
) AUTO_INCREMENT=3;

DELIMITER //

CREATE TRIGGER before_insert_user_sequence
    BEFORE INSERT ON user_sequence
    FOR EACH ROW
BEGIN
    IF NEW.created_at IS NULL THEN
        SET NEW.created_at = NOW();
    END IF;
END;

//
DELIMITER ;

UPDATE user
SET business_name = 'Mi Empresa S.A.',
    phone_number = '123456789'
WHERE username = 'usuario_demo';
