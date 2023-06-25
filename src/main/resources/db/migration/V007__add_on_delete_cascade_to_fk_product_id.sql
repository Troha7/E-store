ALTER TABLE e_store.order_item
DROP CONSTRAINT IF EXISTS product_id_fkey;

ALTER TABLE e_store.order_item
ADD CONSTRAINT product_id_fkey
FOREIGN KEY (fk_product_id) REFERENCES e_store.product (id) ON DELETE CASCADE;