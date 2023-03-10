-- Creating a table
CREATE TABLE IF NOT EXISTS e_store.user_role
(
    id              SERIAL CONSTRAINT user_role_id_pkey PRIMARY KEY,
    fk_user_id      INTEGER CONSTRAINT user_id_fkey
                    REFERENCES e_store.user (id) ON DELETE CASCADE,
    fk_role_id      INTEGER CONSTRAINT role_id_fkey
                    REFERENCES e_store.role (id)
);