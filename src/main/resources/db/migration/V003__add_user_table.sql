-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.user
(
    id              SERIAL CONSTRAINT user_id_pkey PRIMARY KEY,
    name            VARCHAR(25) NOT NULL UNIQUE,
    email           VARCHAR(45) NOT NULL UNIQUE,
    phone           VARCHAR(13) NOT NULL UNIQUE,
    password        VARCHAR(64) NOT NULL
);