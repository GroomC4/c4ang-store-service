-- Store Repository 통합 테스트용 초기 데이터

-- Store 1: REGISTERED 상태
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111113',
        '11111111-1111-1111-1111-111111111111',
        '김사장 스토어',
        '멋진 스토어입니다',
        'REGISTERED',
        NOW(),
        NOW());

-- Store 2: DELETED 상태
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at, deleted_at)
VALUES ('22222222-2222-2222-2222-222222222224',
        '22222222-2222-2222-2222-222222222222',
        '삭제된 스토어',
        '삭제됨',
        'DELETED',
        NOW(),
        NOW(),
        NOW());

-- Store 3: SUSPENDED 상태
INSERT INTO p_store (id, owner_user_id, name, description, status, created_at, updated_at)
VALUES ('33333333-3333-3333-3333-333333333335',
        '33333333-3333-3333-3333-333333333333',
        '일시정지된 스토어',
        '일시정지됨',
        'SUSPENDED',
        NOW(),
        NOW());
