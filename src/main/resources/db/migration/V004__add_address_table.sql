-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.address
(
    id              SERIAL CONSTRAINT address_id_pkey PRIMARY KEY,
    fk_user_id      BIGINT CONSTRAINT user_id_fkey
                    REFERENCES e_store.user (id) ON DELETE CASCADE,
    city            VARCHAR(15) NOT NULL,
    street          VARCHAR(15) NOT NULL,
    house           VARCHAR(5) NOT NULL
);