-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.address
(
    id              SERIAL CONSTRAINT address_id_pkey PRIMARY KEY,
    city            VARCHAR(15) NOT NULL,
    street          VARCHAR(15) NOT NULL,
    house           VARCHAR(5) NOT NULL
);