-- Cleanup script for Contract Tests
-- Delete all test data in reverse order of dependencies

DELETE FROM p_store_rating;
DELETE FROM p_store;
