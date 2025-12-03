-- Test data for Contract Tests
-- Using fixed UUIDs for predictable contract testing

-- Test Store 1: Store for GET /api/v1/stores/{storeId} contract
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('750e8400-e29b-41d4-a716-446655440101', '750e8400-e29b-41d4-a716-446655440001',
        'Contract Test Store', 'A test store for contract verification', 'REGISTERED',
        '2024-01-01 00:00:00', '2024-01-01 00:00:00');

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('750e8400-e29b-41d4-a716-446655440201', '750e8400-e29b-41d4-a716-446655440101',
        4.5, 20, '2024-01-01 00:00:00', '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- Test Store 2: Store for GET /api/v1/stores/mine contract (owner-based query)
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('750e8400-e29b-41d4-a716-446655440102', '750e8400-e29b-41d4-a716-446655440002',
        'My Contract Store', 'Owner test store', 'REGISTERED',
        '2024-01-01 00:00:00', '2024-01-01 00:00:00');

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('750e8400-e29b-41d4-a716-446655440202', '750e8400-e29b-41d4-a716-446655440102',
        3.8, 15, '2024-01-01 00:00:00', '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- Test Store 3: Store for UPDATE/DELETE contracts (owned by specific user)
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('750e8400-e29b-41d4-a716-446655440103', '750e8400-e29b-41d4-a716-446655440003',
        'Modifiable Store', 'Store for update/delete tests', 'REGISTERED',
        '2024-01-01 00:00:00', '2024-01-01 00:00:00');

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('750e8400-e29b-41d4-a716-446655440203', '750e8400-e29b-41d4-a716-446655440103',
        4.2, 8, '2024-01-01 00:00:00', '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- Test Store 4: Store for Internal API contracts (Order Service용)
-- checkStoreExists, getStoreById contracts에서 사용
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440011',
        'Contract Test Store for Order', 'Store for order service internal API contract tests', 'REGISTERED',
        '2024-01-01 00:00:00', '2024-01-01 00:00:00');

INSERT INTO p_store_rating (id, store_id, average_rating, review_count, launched_at, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440021', '660e8400-e29b-41d4-a716-446655440001',
        4.0, 10, '2024-01-01 00:00:00', '2024-01-01 00:00:00', '2024-01-01 00:00:00');
