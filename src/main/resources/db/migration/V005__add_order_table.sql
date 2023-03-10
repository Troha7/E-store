-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.order
(
    id              SERIAL CONSTRAINT order_id_pkey PRIMARY KEY,
    fk_user_id      INTEGER CONSTRAINT user_id_fkey
                    REFERENCES e_store.user (id) ON DELETE CASCADE,
    order_date      DATE NOT NULL DEFAULT CURRENT_DATE
);