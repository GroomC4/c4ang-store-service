-- Test data for RegisterStoreServiceIntegrationTest
-- Using fixed UUIDs for predictable testing

-- Test Store: Existing store for duplicate test
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccc02', 'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Existing Store', 'Already exists', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccc03', 'cccccccc-cccc-cccc-cccc-cccccccccc02',
        0.00, 0, NOW(), NOW(), NOW());
