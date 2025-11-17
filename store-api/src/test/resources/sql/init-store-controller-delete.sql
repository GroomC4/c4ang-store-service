-- Test data for StoreControllerIntegrationTest - Delete Store Tests
-- Using fixed UUIDs for predictable testing

-- Test Store 1: Store for successful delete
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555571', '11111111-2222-3333-4444-555555555551',
        'Delete Test Store 1', 'To be deleted', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555581', '11111111-2222-3333-4444-555555555571',
        0.00, 0, NOW(), NOW(), NOW());

-- Test Store 2: Already deleted store for re-delete test
INSERT INTO p_store (id, owner_user_id, name, description, status, deleted_at, created_at, updated_at)
VALUES ('22222222-2222-3333-4444-555555555572', '22222222-2222-3333-4444-555555555552',
        'Already Deleted Store', 'Already deleted', 'DELETED', NOW(), NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('22222222-2222-3333-4444-555555555582', '22222222-2222-3333-4444-555555555572',
        0.00, 0, NOW(), NOW(), NOW());
