-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.role
(
    id              SERIAL CONSTRAINT role_id_pkey PRIMARY KEY,
    name            VARCHAR(15) NOT NULL
);