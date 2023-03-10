-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.user
(
    id              SERIAL CONSTRAINT user_id_pkey PRIMARY KEY,
    fk_address_id   INTEGER CONSTRAINT address_id_fkey
                    REFERENCES e_store.address (id) ON DELETE CASCADE,
    name            VARCHAR(25) NOT NULL UNIQUE,
    email           VARCHAR(45) NOT NULL UNIQUE,
    phone           VARCHAR(10) NOT NULL UNIQUE,
    password        VARCHAR(64) NOT NULL
);