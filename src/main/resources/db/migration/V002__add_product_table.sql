-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.product
(
    id          SERIAL CONSTRAINT product_id_pkey PRIMARY KEY,
    name        VARCHAR(25) NOT NULL,
    description VARCHAR(64) NOT NULL,
    price       DECIMAL(12, 2) NOT NULL
);