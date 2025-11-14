-- Test data for GetStoreServiceIntegrationTest
-- Using fixed UUIDs for predictable testing

-- Test Store 1: Active store
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111113', '11111111-1111-1111-1111-111111111111',
        '김사장 스토어', '멋진 스토어입니다', 'REGISTERED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111114', '11111111-1111-1111-1111-111111111113',
        4.5, 100, NOW(), NOW(), NOW());

-- Test Store 2: Deleted store
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at, deleted_at)
VALUES ('22222222-2222-2222-2222-222222222224', '22222222-2222-2222-2222-222222222222',
        '삭제된 스토어', '삭제됨', 'DELETED', NOW(), NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('22222222-2222-2222-2222-222222222225', '22222222-2222-2222-2222-222222222224',
        4.5, 100, NOW(), NOW(), NOW());

-- Test Store 3: Suspended store
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('33333333-3333-3333-3333-333333333335', '33333333-3333-3333-3333-333333333333',
        '일시정지된 스토어', '일시정지됨', 'SUSPENDED', NOW(), NOW());

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('33333333-3333-3333-3333-333333333336', '33333333-3333-3333-3333-333333333335',
        4.5, 100, NOW(), NOW(), NOW());
