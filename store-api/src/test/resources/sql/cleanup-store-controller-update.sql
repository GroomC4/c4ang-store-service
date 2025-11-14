-- Cleanup script for StoreControllerIntegrationTest - Update Store Tests
-- Delete only test data created by init-store-controller-update.sql
-- Delete in order to respect foreign key constraints

-- Delete store ratings
DELETE FROM p_store_rating WHERE id IN (
    '11111111-2222-3333-4444-555555555581',
    '22222222-2222-3333-4444-555555555582'
);

-- Delete stores
DELETE FROM p_store WHERE id IN (
    '11111111-2222-3333-4444-555555555571',
    '22222222-2222-3333-4444-555555555572'
);
