-- Test data for DeleteStoreServiceIntegrationTest
-- Using fixed UUIDs for predictable testing

-- Test Store 1: Store for successful delete
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Store to Delete 1', 'Will be deleted', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa03', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02',
        0.00, 0, NOW(), NOW(), NOW());

-- Test Store 2: Store for access denied test
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Store to Delete 2', 'Owner 2 store', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02',
        0.00, 0, NOW(), NOW(), NOW());

-- Test Store 3: Already deleted store
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'Already Deleted Store', 'Already deleted', 'DELETED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee03', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02',
        0.00, 0, NOW(), NOW(), NOW());
