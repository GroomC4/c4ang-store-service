-- Cleanup script for RegisterStoreServiceIntegrationTest
-- Delete in order to respect foreign key constraints

DELETE FROM p_store_audit;
DELETE FROM p_store_rating;
DELETE FROM p_store;
