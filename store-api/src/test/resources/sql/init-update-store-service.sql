-- Test data for UpdateStoreServiceIntegrationTest
-- Using fixed UUIDs for predictable testing

-- Test Store 1: Store for successful update
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Original Store 1', 'Original Description 1', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa03', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02',
        0.00, 0, NOW(), NOW(), NOW());

-- Test Store 2: Store for null description test
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Original Store 2', 'Original Description 2', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02',
        0.00, 0, NOW(), NOW(), NOW());

-- Test Store 3: Store for access denied test
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccc02', 'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Original Store 3', 'Original Description 3', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccc03', 'cccccccc-cccc-cccc-cccc-cccccccccc02',
        0.00, 0, NOW(), NOW(), NOW());

-- Test Store 4: Store for name-only update test
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('ffffffff-ffff-ffff-ffff-ffffffffff02', 'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'Original Store 6', 'Keep this description', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('ffffffff-ffff-ffff-ffff-ffffffffff03', 'ffffffff-ffff-ffff-ffff-ffffffffff02',
        0.00, 0, NOW(), NOW(), NOW());
