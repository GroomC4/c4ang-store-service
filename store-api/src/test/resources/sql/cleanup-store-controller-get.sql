-- Cleanup script for StoreControllerIntegrationTest - Get Store Tests
-- Delete only test data created by init-store-controller-get.sql
-- Delete in order to respect foreign key constraints

-- Delete store ratings
DELETE FROM p_store_rating WHERE id = '11111111-2222-3333-4444-555555555581';

-- Delete stores
DELETE FROM p_store WHERE id = '11111111-2222-3333-4444-555555555571';
