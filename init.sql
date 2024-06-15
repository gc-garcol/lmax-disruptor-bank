CREATE DATABASE p2pc;
CREATE USER 'reader'@'%' IDENTIFIED BY 'reader';
GRANT SELECT ON p2pc.* TO 'reader'@'%';
FLUSH PRIVILEGES;

CREATE TABLE p2pc.balances
(
    id          BIGINT PRIMARY KEY,
    amount      BIGINT  NOT NULL DEFAULT 0,
    `precision` INT     NOT NULL DEFAULT 2,
    active      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE p2pc.snapshots
(
    `id`  VARCHAR(36) PRIMARY KEY,
    value VARCHAR(255) NOT NULL
);

INSERT INTO p2pc.snapshots (`id`, `value`) VALUES ('LAST_KAFKA_OFFSET', '-1');
INSERT INTO p2pc.snapshots (`id`, `value`) VALUES ('LAST_BALANCE_ID', '0');
