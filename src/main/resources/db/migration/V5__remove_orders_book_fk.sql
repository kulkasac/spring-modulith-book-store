-- Removing FK from orders.book_id → books.id
-- orders and inventory are separate modules and must not share DB constraints.
-- book_id in orders is intentionally a plain UUID reference, not a FK.
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_book_id_fkey;