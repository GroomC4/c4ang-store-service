-- Test data for StoreControllerIntegrationTest - Get Store Tests
-- Using fixed UUIDs for predictable testing

-- Test Store 1: Store for successful get
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555571', '11111111-2222-3333-4444-555555555551',
        'Get Test Store 1', 'Test Description', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555581', '11111111-2222-3333-4444-555555555571',
        4.50, 10, NOW(), NOW(), NOW());
