-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.user
(
    id              SERIAL CONSTRAINT user_id_pkey PRIMARY KEY,
    username        VARCHAR(64) NOT NULL UNIQUE,
    password        VARCHAR(2048) NOT NULL,
    role            VARCHAR(32) NOT NULL,
    first_name      VARCHAR(64) NOT NULL,
    last_name       VARCHAR(64) NOT NULL,
    email           VARCHAR(64) NOT NULL UNIQUE,
    phone           VARCHAR(13) NOT NULL UNIQUE

);