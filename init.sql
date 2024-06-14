CREATE DATABASE p2pc;
USE p2pc;

CREATE TABLE balances
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount    BIGINT NOT NULL,
    precision INT    NOT NULL DEFAULT 2
);
