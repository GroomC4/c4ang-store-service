-- Cleanup script for StoreControllerIntegrationTest - Register Store Tests
-- Delete only test data created by init-store-controller-register.sql
-- Delete in order to respect foreign key constraints

-- Delete any stores created during tests
DELETE FROM p_store_rating WHERE store_id IN (
    SELECT id FROM p_store WHERE owner_user_id IN (
        'aaaaaaaa-1111-2222-3333-444444444441',
        'bbbbbbbb-1111-2222-3333-444444444442'
    )
);

DELETE FROM p_store WHERE owner_user_id IN (
    'aaaaaaaa-1111-2222-3333-444444444441',
    'bbbbbbbb-1111-2222-3333-444444444442'
);
