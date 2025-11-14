-- Cleanup all tables in correct order (respecting foreign key constraints)
-- Order: child tables first, parent tables last

-- Store related tables
DELETE FROM p_store_audit;
DELETE FROM p_store_rating;
DELETE FROM p_store;
