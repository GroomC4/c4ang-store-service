-- Test data for StoreControllerIntegrationTest - Update Store Tests
-- Using fixed UUIDs for predictable testing

-- Test Store 1: Store for successful update
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555571', '11111111-2222-3333-4444-555555555551',
        'Update Test Store 1', 'Original Description', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555581', '11111111-2222-3333-4444-555555555571',
        0.00, 0, NOW(), NOW(), NOW());

-- Test Store 2: Store for access denied test
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('22222222-2222-3333-4444-555555555572', '22222222-2222-3333-4444-555555555552',
        'Update Test Store 2', 'Store owned by owner2', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('22222222-2222-3333-4444-555555555582', '22222222-2222-3333-4444-555555555572',
        0.00, 0, NOW(), NOW(), NOW());
