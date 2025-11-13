-- Marketplace platform DDL generated from database_design_requirent.md requirements
-- PostgreSQL dialect

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Core user tables
CREATE TABLE IF NOT EXISTS p_user (
    id                UUID PRIMARY KEY,
    username          VARCHAR(10) NOT NULL,
    email             TEXT NOT NULL,
    password_hash     TEXT NOT NULL,
    role              TEXT NOT NULL CHECK (role IN ('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at     TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (email, role),
    CHECK (char_length(username) BETWEEN 2 AND 10)
);

COMMENT ON TABLE p_user IS '인증, 권한, 라이프사이클을 관리하는 사용자 계정 테이블.';
COMMENT ON COLUMN p_user.id IS 'UUID 기본 키.';
COMMENT ON COLUMN p_user.username IS '4~10자 영문 소문자와 숫자로 구성된 로그인 아이디.';
COMMENT ON COLUMN p_user.email IS '알림과 로그인에 사용하는 고유 이메일 주소.';
COMMENT ON COLUMN p_user.password_hash IS 'BCrypt로 암호화된 비밀번호 해시.';
COMMENT ON COLUMN p_user.role IS 'CUSTOMER/OWNER/MANAGER/MASTER 등 사용자 권한 역할.';
COMMENT ON COLUMN p_user.is_active IS '계정 삭제 없이 로그인을 비활성화할 때 사용하는 플래그.';
COMMENT ON COLUMN p_user.last_login_at IS '마지막 로그인 성공 시각.';
COMMENT ON COLUMN p_user.created_at IS '사용자 레코드 생성 시각.';
COMMENT ON COLUMN p_user.updated_at IS '사용자 레코드 마지막 수정 시각.';
COMMENT ON COLUMN p_user.deleted_at IS '소프트 삭제 시각(NULL이면 사용 중).';

CREATE TABLE IF NOT EXISTS p_user_refresh_token (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    token           TEXT NULL,
    client_ip       TEXT,
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- 단일 디바이스 로그인 제약 (향후 제거 가능)
    UNIQUE (user_id)
);

CREATE INDEX idx_refresh_token_user_id ON p_user_refresh_token(user_id);
CREATE INDEX idx_refresh_token_token ON p_user_refresh_token(token);
CREATE INDEX idx_refresh_token_expires_at ON p_user_refresh_token(expires_at);

COMMENT ON TABLE p_user_refresh_token IS 'JWT Refresh Token 저장 테이블. 단일/멀티 디바이스 로그인 관리.';
COMMENT ON COLUMN p_user_refresh_token.id IS 'UUID 기본 키.';
COMMENT ON COLUMN p_user_refresh_token.user_id IS 'p_user.id를 논리적으로 참조하는 사용자 ID.';
COMMENT ON COLUMN p_user_refresh_token.token IS 'JWT Refresh Token 문자열.';
COMMENT ON COLUMN p_user_refresh_token.client_ip IS '토큰 발급 시 클라이언트 IP 주소.';
COMMENT ON COLUMN p_user_refresh_token.expires_at IS 'Refresh Token 만료 시각.';
COMMENT ON COLUMN p_user_refresh_token.created_at IS '토큰 최초 생성 시각.';
COMMENT ON COLUMN p_user_refresh_token.updated_at IS '토큰 갱신 시각 (로그인 시 덮어쓰기).';

CREATE TABLE IF NOT EXISTS p_user_profile (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL,
    full_name         TEXT NOT NULL,
    phone_number      TEXT NOT NULL,
    contact_email     TEXT,
    default_address   TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (user_id)
);

COMMENT ON TABLE p_user_profile IS 'p_user와 1:1로 매핑되는 확장 프로필 정보 테이블.';
COMMENT ON COLUMN p_user_profile.id IS '프로필 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_user_profile.user_id IS 'FK 없이 논리적으로 연결하는 사용자 식별자.';
COMMENT ON COLUMN p_user_profile.full_name IS '주문 및 커뮤니케이션에 노출되는 실명.';
COMMENT ON COLUMN p_user_profile.phone_number IS '대표 연락처 전화번호.';
COMMENT ON COLUMN p_user_profile.contact_email IS '선택 입력 가능한 보조 이메일.';
COMMENT ON COLUMN p_user_profile.default_address IS '기본 배송지 스냅샷 문자열.';
COMMENT ON COLUMN p_user_profile.created_at IS '프로필 생성 시각.';
COMMENT ON COLUMN p_user_profile.updated_at IS '프로필 최종 수정 시각.';
COMMENT ON COLUMN p_user_profile.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_user_address (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL,
    label             TEXT NOT NULL,
    recipient_name    TEXT NOT NULL,
    phone_number      TEXT NOT NULL,
    postal_code       TEXT NOT NULL,
    address_line1     TEXT NOT NULL,
    address_line2     TEXT,
    is_default        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (user_id, label)
);

COMMENT ON TABLE p_user_address IS '사용자별 배송/결제 주소 목록을 관리하는 테이블.';
COMMENT ON COLUMN p_user_address.id IS '주소 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_user_address.user_id IS '주소를 소유한 사용자 식별자.';
COMMENT ON COLUMN p_user_address.label IS '사용자가 부여한 주소 별칭(예: 집, 회사).';
COMMENT ON COLUMN p_user_address.recipient_name IS '배송 수취인 이름.';
COMMENT ON COLUMN p_user_address.phone_number IS '수취인 연락처 전화번호.';
COMMENT ON COLUMN p_user_address.postal_code IS '주소의 우편번호.';
COMMENT ON COLUMN p_user_address.address_line1 IS '기본 도로명 또는 지번 주소.';
COMMENT ON COLUMN p_user_address.address_line2 IS '동/호 등 추가 상세 주소.';
COMMENT ON COLUMN p_user_address.is_default IS '기본 배송지 여부 플래그.';
COMMENT ON COLUMN p_user_address.created_at IS '주소 생성 시각.';
COMMENT ON COLUMN p_user_address.updated_at IS '주소 최종 수정 시각.';
COMMENT ON COLUMN p_user_address.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_user_audit (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL,
    event_type        TEXT NOT NULL CHECK (event_type IN ('USER_REGISTERED', 'PROFILE_UPDATED')),
    change_summary    TEXT,
    recorded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata          JSONB
);

COMMENT ON TABLE p_user_audit IS '사용자 계정의 주요 변경 이력을 남기는 감사 로그.';
COMMENT ON COLUMN p_user_audit.id IS '감사 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_user_audit.user_id IS '감사 이벤트의 대상 사용자 식별자.';
COMMENT ON COLUMN p_user_audit.event_type IS '회원가입, 프로필 변경 등 이벤트 유형.';
COMMENT ON COLUMN p_user_audit.change_summary IS '변경 내용을 요약한 설명.';
COMMENT ON COLUMN p_user_audit.recorded_at IS '감사 이벤트가 기록된 시각.';
COMMENT ON COLUMN p_user_audit.metadata IS '추가 메타 정보를 담는 JSON 데이터.';

-- Store domain
CREATE TABLE IF NOT EXISTS p_store (
    id                UUID PRIMARY KEY,
    owner_user_id     UUID NOT NULL,
    name              TEXT NOT NULL,
    description       TEXT,
    status            TEXT NOT NULL DEFAULT 'REGISTERED' CHECK (status IN ('REGISTERED', 'SUSPENDED', 'DELETED')),
    hidden_at         TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (owner_user_id),
    CHECK (hidden_at IS NULL OR hidden_at >= created_at)
);

COMMENT ON TABLE p_store IS '판매자가 운영하는 스토어 기본 정보를 담는 테이블.';
COMMENT ON COLUMN p_store.id IS '스토어의 UUID 기본 키.';
COMMENT ON COLUMN p_store.owner_user_id IS '스토어를 소유한 판매자 사용자 UUID.';
COMMENT ON COLUMN p_store.name IS '스토어 표시 이름.';
COMMENT ON COLUMN p_store.description IS '스토어 소개 및 설명.';
COMMENT ON COLUMN p_store.status IS 'REGISTERED, SUSPENDED 등 스토어 상태 값.';
COMMENT ON COLUMN p_store.hidden_at IS '스토어가 노출 중단된 시각.';
COMMENT ON COLUMN p_store.created_at IS '스토어 생성 시각.';
COMMENT ON COLUMN p_store.updated_at IS '스토어 최종 수정 시각.';
COMMENT ON COLUMN p_store.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_store_rating (
    id                UUID PRIMARY KEY,
    store_id          UUID NOT NULL,
    average_rating    NUMERIC(3, 2) NOT NULL DEFAULT 0,
    review_count      INTEGER NOT NULL DEFAULT 0,
    launched_at       TIMESTAMPTZ NOT NULL,
    hidden_at         TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (store_id),
    CHECK (average_rating BETWEEN 0 AND 5),
    CHECK (review_count >= 0)
);

COMMENT ON TABLE p_store_rating IS '스토어의 평점과 리뷰 집계 데이터를 저장하는 테이블.';
COMMENT ON COLUMN p_store_rating.id IS '스토어 평점 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_store_rating.store_id IS '평점이 속한 스토어 ID.';
COMMENT ON COLUMN p_store_rating.average_rating IS '5점 만점 기준 평균 평점.';
COMMENT ON COLUMN p_store_rating.review_count IS '평균에 반영된 노출 리뷰 수.';
COMMENT ON COLUMN p_store_rating.launched_at IS '스토어 오픈(런칭) 시각.';
COMMENT ON COLUMN p_store_rating.hidden_at IS '평점 노출이 중단된 시각.';
COMMENT ON COLUMN p_store_rating.created_at IS '평점 레코드 생성 시각.';
COMMENT ON COLUMN p_store_rating.updated_at IS '평점 레코드 최종 수정 시각.';
COMMENT ON COLUMN p_store_rating.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_store_audit (
    id                UUID PRIMARY KEY,
    store_id          UUID NOT NULL,
    event_type        TEXT NOT NULL CHECK (event_type IN ('REGISTERED', 'INFO_UPDATED', 'SUSPENDED', 'DELETED')),
    status_snapshot   TEXT CHECK (status_snapshot IN ('REGISTERED', 'SUSPENDED', 'DELETED')),
    change_summary    TEXT,
    actor_user_id     UUID,
    recorded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata          JSONB
);

COMMENT ON TABLE p_store_audit IS '스토어 등록/정지/삭제 등 상태 변화를 기록하는 감사 로그.';
COMMENT ON COLUMN p_store_audit.id IS '스토어 감사 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_store_audit.store_id IS '이벤트가 발생한 스토어 ID.';
COMMENT ON COLUMN p_store_audit.event_type IS 'REGISTERED, SUSPENDED 등 이벤트 유형.';
COMMENT ON COLUMN p_store_audit.status_snapshot IS '이벤트 시점의 스토어 상태 스냅샷.';
COMMENT ON COLUMN p_store_audit.change_summary IS '변경 사항 요약 설명.';
COMMENT ON COLUMN p_store_audit.actor_user_id IS '변경을 수행한 사용자 UUID(있는 경우).';
COMMENT ON COLUMN p_store_audit.recorded_at IS '감사 이벤트 기록 시각.';
COMMENT ON COLUMN p_store_audit.metadata IS '추가 메타 정보를 담은 JSON.';

-- Product domain
CREATE TABLE IF NOT EXISTS p_product_category (
    id                UUID PRIMARY KEY,
    name              TEXT NOT NULL,
    parent_category_id UUID,
    depth             INTEGER NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (name, parent_category_id),
    CHECK (depth >= 0)
);

COMMENT ON TABLE p_product_category IS '상품 카테고리의 계층 구조를 관리하는 테이블.';
COMMENT ON COLUMN p_product_category.id IS '카테고리 노드의 UUID 기본 키.';
COMMENT ON COLUMN p_product_category.name IS '카테고리 표시 이름.';
COMMENT ON COLUMN p_product_category.parent_category_id IS '상위 카테고리 ID(NULL이면 루트).';
COMMENT ON COLUMN p_product_category.depth IS '루트 0부터 시작하는 계층 깊이.';
COMMENT ON COLUMN p_product_category.created_at IS '카테고리 생성 시각.';
COMMENT ON COLUMN p_product_category.updated_at IS '카테고리 최종 수정 시각.';
COMMENT ON COLUMN p_product_category.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_product (
    id                UUID PRIMARY KEY,
    store_id          UUID NOT NULL,
    store_name        TEXT NOT NULL,
    category_id       UUID NOT NULL,
    product_name      TEXT NOT NULL,
    status            TEXT NOT NULL DEFAULT 'ON_SALE' CHECK (status IN ('ON_SALE', 'OFF_SHELF')),
    price             NUMERIC(12, 2) NOT NULL,
    stock_quantity    INTEGER NOT NULL DEFAULT 0,
    thumbnail_url     TEXT,
    description       TEXT,
    hidden_at         TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    CHECK (price >= 0),
    CHECK (stock_quantity >= 0)
);

COMMENT ON TABLE p_product IS '판매 가능한 상품 정보를 저장하는 테이블.';
COMMENT ON COLUMN p_product.id IS '상품의 UUID 기본 키.';
COMMENT ON COLUMN p_product.store_id IS '상품을 판매하는 스토어 ID.';
COMMENT ON COLUMN p_product.store_name IS '스토어 이름 (비정규화, 읽기 최적화용).';
COMMENT ON COLUMN p_product.category_id IS '주요 카테고리 식별자.';
COMMENT ON COLUMN p_product.product_name IS '상품명.';
COMMENT ON COLUMN p_product.status IS '판매 상태(ON_SALE/OFF_SHELF).';
COMMENT ON COLUMN p_product.price IS '현재 판매 가격.';
COMMENT ON COLUMN p_product.stock_quantity IS '잔여 재고 수량.';
COMMENT ON COLUMN p_product.thumbnail_url IS '대표 이미지 URL.';
COMMENT ON COLUMN p_product.description IS '상품 상세 설명.';
COMMENT ON COLUMN p_product.hidden_at IS '상품이 숨김 처리된 시각.';
COMMENT ON COLUMN p_product.created_at IS '상품 등록 시각.';
COMMENT ON COLUMN p_product.updated_at IS '상품 정보 최종 수정 시각.';
COMMENT ON COLUMN p_product.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_product_image (
    id                UUID PRIMARY KEY,
    product_id        UUID NOT NULL,
    image_type        TEXT NOT NULL CHECK (image_type IN ('PRIMARY', 'DETAIL')),
    image_url         TEXT NOT NULL,
    display_order     INTEGER NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (product_id, image_type, display_order)
);

COMMENT ON TABLE p_product_image IS '상품의 상세/대표 이미지를 관리하는 테이블.';
COMMENT ON COLUMN p_product_image.id IS '상품 이미지의 UUID 기본 키.';
COMMENT ON COLUMN p_product_image.product_id IS '이미지가 연결된 상품 ID.';
COMMENT ON COLUMN p_product_image.image_type IS 'PRIMARY/DETAIL 등 이미지 용도.';
COMMENT ON COLUMN p_product_image.image_url IS '이미지 파일 URL.';
COMMENT ON COLUMN p_product_image.display_order IS '0부터 시작하는 노출 순서.';
COMMENT ON COLUMN p_product_image.created_at IS '이미지 레코드 생성 시각.';
COMMENT ON COLUMN p_product_image.updated_at IS '이미지 레코드 최종 수정 시각.';
COMMENT ON COLUMN p_product_image.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_product_audit (
    id                UUID PRIMARY KEY,
    product_id        UUID NOT NULL,
    actor_user_id     UUID,
    event_type        TEXT NOT NULL CHECK (event_type IN ('PRODUCT_REGISTERED', 'PRODUCT_HIDDEN', 'PRODUCT_DELETED', 'PRODUCT_UPDATED')),
    status_snapshot   TEXT CHECK (status_snapshot IN ('ON_SALE', 'OFF_SHELF')),
    hidden_at_snapshot TIMESTAMPTZ,
    change_summary    TEXT,
    recorded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata          JSONB
);

COMMENT ON TABLE p_product_audit IS '상품 등록/숨김/삭제 등 변동 내역을 기록하는 감사 로그.';
COMMENT ON COLUMN p_product_audit.id IS '상품 감사 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_product_audit.product_id IS '이벤트 대상 상품 ID.';
COMMENT ON COLUMN p_product_audit.actor_user_id IS '변경 수행자 사용자 UUID.';
COMMENT ON COLUMN p_product_audit.event_type IS 'PRODUCT_REGISTERED 등 이벤트 유형.';
COMMENT ON COLUMN p_product_audit.status_snapshot IS '이벤트 시점의 상품 상태 스냅샷.';
COMMENT ON COLUMN p_product_audit.hidden_at_snapshot IS '이벤트 기록 당시 hidden_at 값.';
COMMENT ON COLUMN p_product_audit.change_summary IS '변경 사항 요약 설명.';
COMMENT ON COLUMN p_product_audit.recorded_at IS '감사 이벤트 기록 시각.';
COMMENT ON COLUMN p_product_audit.metadata IS '추가 정보를 담은 JSON 메타데이터.';

-- Order domain
CREATE TABLE IF NOT EXISTS p_order (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL,
    store_id          UUID NOT NULL,
    order_number      TEXT NOT NULL,
    status            TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN (
        -- 주문 접수 단계
        'PENDING',
        'STOCK_RESERVED',
        -- 결제 단계
        'PAYMENT_PENDING',
        'PAYMENT_PROCESSING',
        'PAYMENT_COMPLETED',
        -- 배송 단계
        'PREPARING',
        'SHIPPED',
        'DELIVERED',
        -- 취소/실패 단계
        'PAYMENT_TIMEOUT',
        'ORDER_CANCELLED',
        -- 반품/환불 단계
        'RETURN_REQUESTED',
        'RETURN_APPROVED',
        'RETURN_IN_TRANSIT',
        'RETURN_COMPLETED',
        'REFUND_PROCESSING',
        'REFUND_COMPLETED',
        -- 예외 처리
        'FAILED',
        'REQUIRES_MANUAL_INTERVENTION'
    )),
    payment_summary   JSON NOT NULL,
    timeline          JSON NOT NULL,
    note              TEXT,
    -- 비동기 플로우 추가 컬럼
    reservation_id    TEXT,
    payment_id        UUID,
    expires_at        TIMESTAMPTZ,
    confirmed_at      TIMESTAMPTZ,
    cancelled_at      TIMESTAMPTZ,
    failure_reason    TEXT,
    refund_id         TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (order_number)
);

COMMENT ON TABLE p_order IS '주문 요약 정보를 담는 테이블.';
COMMENT ON COLUMN p_order.id IS '주문의 UUID 기본 키.';
COMMENT ON COLUMN p_order.user_id IS '주문한 사용자 UUID.';
COMMENT ON COLUMN p_order.store_id IS '주문이 속한 스토어 ID.';
COMMENT ON COLUMN p_order.status IS '주문의 현재 상태(주문중, 결제완료, 배송준비, 배송중, 배송완료, 주문취소, 반품/환불요청, 반품/환불완료)';
COMMENT ON COLUMN p_order.payment_summary IS '결제 금액 등 결제 요약 정보를 담은 JSON 객체.';
COMMENT ON COLUMN p_order.timeline IS '주문 상태 변경 이력을 담은 JSON 배열.';
COMMENT ON COLUMN p_order.order_number IS '고객에게 안내되는 주문 번호. 형식은 ORD-주문연월-6자리난수(예: ORD-20251017-X8Z1Y9)';
COMMENT ON COLUMN p_order.note IS '주문 관련 메모 또는 요청 사항.';
COMMENT ON COLUMN p_order.reservation_id IS '재고 예약 시스템의 예약 ID (Redis key)';
COMMENT ON COLUMN p_order.payment_id IS '연결된 결제 레코드 ID';
COMMENT ON COLUMN p_order.expires_at IS '결제 시간 제한 (주문 생성 후 10분)';
COMMENT ON COLUMN p_order.confirmed_at IS '주문 확정 시각 (결제 완료 시)';
COMMENT ON COLUMN p_order.cancelled_at IS '주문 취소 시각';
COMMENT ON COLUMN p_order.failure_reason IS '실패 또는 취소 사유';
COMMENT ON COLUMN p_order.refund_id IS '환불 거래 ID (PG사 제공)';
COMMENT ON COLUMN p_order.created_at IS '주문 레코드 생성 시각.';
COMMENT ON COLUMN p_order.updated_at IS '주문 레코드 최종 수정 시각.';
COMMENT ON COLUMN p_order.deleted_at IS '소프트 삭제 시각.';

-- 성능 최적화 인덱스
CREATE INDEX IF NOT EXISTS idx_p_order_status_expires
ON p_order (status, expires_at)
WHERE status IN ('PAYMENT_PENDING', 'PAYMENT_PROCESSING');

CREATE INDEX IF NOT EXISTS idx_p_order_reservation
ON p_order (reservation_id)
WHERE reservation_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_p_order_payment
ON p_order (payment_id)
WHERE payment_id IS NOT NULL;

COMMENT ON INDEX idx_p_order_status_expires IS '결제 타임아웃 스케줄러 최적화용';
COMMENT ON INDEX idx_p_order_reservation IS '재고 예약 조회 최적화용';
COMMENT ON INDEX idx_p_order_payment IS '결제 콜백 처리 최적화용';

CREATE TABLE IF NOT EXISTS p_order_item (
    id                UUID PRIMARY KEY,
    order_id          UUID NOT NULL,
    product_id        UUID NOT NULL,
    product_name      TEXT NOT NULL,
    quantity          INTEGER NOT NULL,
    unit_price        NUMERIC(12, 2) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (quantity > 0),
    CHECK (unit_price >= 0)
);

COMMENT ON TABLE p_order_item IS '주문에 포함된 개별 상품 라인 아이템 테이블.';
COMMENT ON COLUMN p_order_item.id IS '주문 상품의 UUID 기본 키.';
COMMENT ON COLUMN p_order_item.order_id IS '소속 주문 ID.';
COMMENT ON COLUMN p_order_item.product_id IS '주문된 상품 ID.';
COMMENT ON COLUMN p_order_item.product_name IS '주문된 상품명.';
COMMENT ON COLUMN p_order_item.quantity IS '주문 수량.';
COMMENT ON COLUMN p_order_item.unit_price IS '주문 시점의 단가.';
COMMENT ON COLUMN p_order_item.created_at IS '주문 상품 레코드 생성 시각.';
COMMENT ON COLUMN p_order_item.updated_at IS '주문 상품 레코드 최종 수정 시각.';

CREATE TABLE IF NOT EXISTS p_order_item_shipping (
    id                UUID PRIMARY KEY,
    order_id          UUID NOT NULL,
    order_item_id     UUID NOT NULL,
    product_id        UUID NOT NULL,
    tracking_number   TEXT,
    carrier_code      TEXT,
    address_line1     TEXT NOT NULL,
    address_line2     TEXT,
    recipient_name    TEXT NOT NULL,
    recipient_phone   TEXT NOT NULL,
    postal_code       TEXT NOT NULL,
    status            TEXT NOT NULL DEFAULT 'PREPARING' CHECK (status IN ('PREPARING', 'REQUESTED', 'IN_TRANSIT', 'DELIVERED')),
    shipped_at        TIMESTAMPTZ,
    delivered_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (order_id, order_item_id)
);

COMMENT ON TABLE p_order_item_shipping IS '주문 상품별 배송 정보를 저장하는 테이블.';
COMMENT ON COLUMN p_order_item_shipping.id IS '배송 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_order_item_shipping.order_id IS '배송이 속한 주문 ID.';
COMMENT ON COLUMN p_order_item_shipping.order_item_id IS '배송 대상 주문 상품 ID.';
COMMENT ON COLUMN p_order_item_shipping.product_id IS '배송 대상 상품 ID.';
COMMENT ON COLUMN p_order_item_shipping.tracking_number IS '택배사 송장번호.';
COMMENT ON COLUMN p_order_item_shipping.carrier_code IS '애플리케이션에서 관리하는 택배사 코드.';
COMMENT ON COLUMN p_order_item_shipping.address_line1 IS '기본 배송지 주소.';
COMMENT ON COLUMN p_order_item_shipping.address_line2 IS '추가 배송지 상세.';
COMMENT ON COLUMN p_order_item_shipping.recipient_name IS '수취인 이름.';
COMMENT ON COLUMN p_order_item_shipping.recipient_phone IS '수취인 연락처.';
COMMENT ON COLUMN p_order_item_shipping.postal_code IS '배송지 우편번호.';
COMMENT ON COLUMN p_order_item_shipping.status IS 'PREPARING/DELIVERED 등 배송 상태.';
COMMENT ON COLUMN p_order_item_shipping.shipped_at IS '출고 시각.';
COMMENT ON COLUMN p_order_item_shipping.delivered_at IS '배송 완료 시각.';
COMMENT ON COLUMN p_order_item_shipping.created_at IS '배송 레코드 생성 시각.';
COMMENT ON COLUMN p_order_item_shipping.updated_at IS '배송 레코드 최종 수정 시각.';
COMMENT ON COLUMN p_order_item_shipping.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_order_audit (
    id                UUID PRIMARY KEY,
    order_id          UUID NOT NULL,
    order_item_id     UUID,
    event_type        TEXT NOT NULL CHECK (event_type IN (
        'ORDER_CREATED',
        'STOCK_RESERVED',
        'PAYMENT_REQUESTED',
        'PAYMENT_COMPLETED',
        'ORDER_CONFIRMED',
        'ORDER_CANCELLED',
        'ORDER_REFUNDED',
        'ORDER_TIMEOUT'
    )),
    change_summary    TEXT,
    actor_user_id     UUID,
    recorded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata          JSONB
);

COMMENT ON TABLE p_order_audit IS '주문 및 배송 상태 변화를 추적하는 감사 로그.';
COMMENT ON COLUMN p_order_audit.id IS '주문 감사 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_order_audit.order_id IS '이벤트가 발생한 주문 ID.';
COMMENT ON COLUMN p_order_audit.order_item_id IS '영향을 받은 주문 상품 ID(없을 수 있음).';
COMMENT ON COLUMN p_order_audit.event_type IS 'ORDERED, SHIPPING_IN_TRANSIT 등 이벤트 유형.';
COMMENT ON COLUMN p_order_audit.change_summary IS '변경 사항 요약 설명.';
COMMENT ON COLUMN p_order_audit.actor_user_id IS '변경을 수행한 사용자 UUID.';
COMMENT ON COLUMN p_order_audit.recorded_at IS '감사 이벤트가 기록된 시각.';
COMMENT ON COLUMN p_order_audit.metadata IS '추가 정보를 담은 JSON 메타데이터.';

-- Stock Reservation Log (Redis 재고 예약 백업)
CREATE TABLE IF NOT EXISTS p_stock_reservation_log (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id    TEXT NOT NULL UNIQUE,
    order_id          UUID NOT NULL,
    store_id          UUID NOT NULL,
    products          JSONB NOT NULL,
    status            TEXT NOT NULL CHECK (status IN ('RESERVED', 'CONFIRMED', 'RELEASED', 'EXPIRED')),
    reserved_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at        TIMESTAMPTZ NOT NULL,
    confirmed_at      TIMESTAMPTZ,
    released_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_stock_reservation_order ON p_stock_reservation_log(order_id);
CREATE INDEX IF NOT EXISTS idx_stock_reservation_expires ON p_stock_reservation_log(expires_at) WHERE status = 'RESERVED';
CREATE INDEX IF NOT EXISTS idx_stock_reservation_status ON p_stock_reservation_log(status);

COMMENT ON TABLE p_stock_reservation_log IS 'Redis 재고 예약 내역을 DB에 백업 (모니터링 및 복구용)';
COMMENT ON COLUMN p_stock_reservation_log.reservation_id IS 'Redis에서 사용하는 예약 ID';
COMMENT ON COLUMN p_stock_reservation_log.order_id IS '연결된 주문 ID';
COMMENT ON COLUMN p_stock_reservation_log.store_id IS '스토어 ID';
COMMENT ON COLUMN p_stock_reservation_log.products IS '예약된 상품 목록 [{productId, quantity}]';
COMMENT ON COLUMN p_stock_reservation_log.status IS 'RESERVED(예약), CONFIRMED(확정), RELEASED(해제), EXPIRED(만료)';
COMMENT ON COLUMN p_stock_reservation_log.reserved_at IS '예약 시각';
COMMENT ON COLUMN p_stock_reservation_log.expires_at IS '예약 만료 시각';
COMMENT ON COLUMN p_stock_reservation_log.confirmed_at IS '예약 확정 시각 (결제 완료)';
COMMENT ON COLUMN p_stock_reservation_log.released_at IS '예약 해제 시각 (취소 또는 만료)';

-- Payment domain
CREATE TABLE IF NOT EXISTS p_payment (
    id                    UUID PRIMARY KEY,
    version               BIGINT NOT NULL DEFAULT 0,
    order_id              UUID NOT NULL,
    total_amount          NUMERIC(12, 2),
    payment_amount        NUMERIC(12, 2),
    discount_amount       NUMERIC(12, 2),
    delivery_fee          NUMERIC(12, 2),
    method                TEXT CHECK (method IN ('CARD', 'TOSS_PAY')),
    status                TEXT NOT NULL DEFAULT 'PAYMENT_WAIT' CHECK (status IN ('PAYMENT_WAIT', 'PAYMENT_REQUEST', 'PAYMENT_COMPLETED', 'PAYMENT_FAILED', 'PAYMENT_CANCELLED', 'REFUND_REQUESTED', 'REFUND_COMPLETED')),
    timeline              JSON NOT NULL,
    requested_at          TIMESTAMPTZ NOT NULL,
    completed_at          TIMESTAMPTZ,
    cancelled_at          TIMESTAMPTZ,
    refunded_at           TIMESTAMPTZ,
    pg_transaction_id     TEXT,
    pg_approval_number    TEXT,
    refund_transaction_id TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Business rule: Amounts must be present for non-WAIT status
    CONSTRAINT chk_payment_amount_required CHECK (
        status = 'PAYMENT_WAIT' OR
        (total_amount IS NOT NULL AND
         payment_amount IS NOT NULL AND
         discount_amount IS NOT NULL AND
         delivery_fee IS NOT NULL)
    ),
    CHECK (total_amount IS NULL OR total_amount >= 0)
);

COMMENT ON TABLE p_payment IS '주문 결제 내역을 저장하는 테이블.';
COMMENT ON COLUMN p_payment.id IS '결제 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_payment.order_id IS '결제와 연결된 주문 ID.';
COMMENT ON COLUMN p_payment.total_amount IS '총 주문금액';
COMMENT ON COLUMN p_payment.payment_amount IS '실제 결제된 금액.';
COMMENT ON COLUMN p_payment.discount_amount IS '할인 및 쿠폰 적용 금액.';
COMMENT ON COLUMN p_payment.delivery_fee IS '배송비 금액.';
COMMENT ON COLUMN p_payment.status IS 'PAYMENT_WAIT, PAYMENT_COMPLETED 등 현재 결제 상태.';
COMMENT ON COLUMN p_payment.method IS 'CARD, TOSS_PAY 등 결제 수단.';
COMMENT ON COLUMN p_payment.timeline IS '결제 상태 변경 이력을 담은JSON 배열.';
COMMENT ON COLUMN p_payment.created_at IS '결제 레코드 생성 시각.';
COMMENT ON COLUMN p_payment.updated_at IS '결제 레코드 최종 수정 시각.';

CREATE TABLE IF NOT EXISTS p_payment_gateway_log
(
    id                    UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    payment_id            UUID        NOT NULL,
    pg_code               TEXT        NOT NULL,
    status                TEXT        NOT NULL CHECK (status IN ('REQUEST', 'APPROVED', 'FAILED')),
    external_payment_data TEXT        NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at            TIMESTAMPTZ
);

COMMENT ON TABLE p_payment_gateway_log IS '외부 PG사와 통신한 이력을 저장하는 테이블.';
COMMENT ON COLUMN p_payment_gateway_log.id IS '외부 결제 통신 이력 UUID.';
COMMENT ON COLUMN p_payment_gateway_log.payment_id IS '연결된 결제(p_payment) ID.';
COMMENT ON COLUMN p_payment_gateway_log.pg_code IS 'PG사 식별 코드.';
COMMENT ON COLUMN p_payment_gateway_log.status IS '결제요청, 승인, 실패 상태 값.';
COMMENT ON COLUMN p_payment_gateway_log.external_payment_data IS '요청/응답 전문 원문을 저장한 텍스트.';
COMMENT ON COLUMN p_payment_gateway_log.created_at IS '레코드 생성 시각.';
COMMENT ON COLUMN p_payment_gateway_log.updated_at IS '레코드 최종 수정 시각.';
COMMENT ON COLUMN p_payment_gateway_log.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_payment_history (
    id                UUID PRIMARY KEY,
    payment_id        UUID NOT NULL,
    payment_item_id   UUID,
    event_type        TEXT NOT NULL CHECK (event_type IN (
        'PAYMENT_REQUESTED',
        'PAYMENT_COMPLETED',
        'PAYMENT_FAILED',
        'PAYMENT_CANCELLED',
        'REFUND_REQUESTED',
        'REFUND_COMPLETED'
    )),
    change_summary    TEXT,
    actor_user_id     UUID,
    recorded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata          JSONB
);

COMMENT ON TABLE p_payment_history IS '결제 및 환불 처리 이력을 기록하는 감사 로그.';
COMMENT ON COLUMN p_payment_history.id IS '결제 이력 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_payment_history.payment_id IS '이벤트가 속한 결제 ID.';
COMMENT ON COLUMN p_payment_history.payment_item_id IS '영향을 받은 결제 아이템 ID.';
COMMENT ON COLUMN p_payment_history.event_type IS '결제 이벤트 유형 (PAYMENT_REQUESTED, PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_CANCELLED, REFUND_REQUESTED, REFUND_COMPLETED).';
COMMENT ON COLUMN p_payment_history.change_summary IS '처리 내역 요약 설명.';
COMMENT ON COLUMN p_payment_history.actor_user_id IS '이벤트를 발생시킨 사용자 UUID.';
COMMENT ON COLUMN p_payment_history.recorded_at IS '이력이 기록된 시각.';
COMMENT ON COLUMN p_payment_history.metadata IS 'PG 응답 등 추가 정보를 담은 JSON.';

-- Review domain
CREATE TABLE IF NOT EXISTS p_review (
    id                UUID PRIMARY KEY,
    product_id        UUID NOT NULL,
    user_id           UUID NOT NULL,
    order_id          UUID NOT NULL,
    rating            SMALLINT NOT NULL,
    content           TEXT NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (order_id, product_id),
    CHECK (rating BETWEEN 1 AND 5),
    CHECK (char_length(content) <= 5000)
);

COMMENT ON TABLE p_review IS '구매 완료 사용자만 작성 가능한 상품 리뷰 테이블.';
COMMENT ON COLUMN p_review.id IS '리뷰 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_review.product_id IS '리뷰 대상 상품 ID.';
COMMENT ON COLUMN p_review.user_id IS '리뷰 작성자 사용자 UUID.';
COMMENT ON COLUMN p_review.order_id IS '리뷰가 연결된 주문 ID.';
COMMENT ON COLUMN p_review.rating IS '1~5점 범위의 평점.';
COMMENT ON COLUMN p_review.content IS '최대 5000자의 리뷰 본문.';
COMMENT ON COLUMN p_review.created_at IS '리뷰 작성 시각.';
COMMENT ON COLUMN p_review.updated_at IS '리뷰 최종 수정 시각.';
COMMENT ON COLUMN p_review.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_review_image (
    id                UUID PRIMARY KEY,
    review_id         UUID NOT NULL,
    image_url         TEXT NOT NULL,
    display_order     INTEGER NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    UNIQUE (review_id, display_order)
);

COMMENT ON TABLE p_review_image IS '리뷰에 첨부되는 이미지 목록.';
COMMENT ON COLUMN p_review_image.id IS '리뷰 이미지의 UUID 기본 키.';
COMMENT ON COLUMN p_review_image.review_id IS '이미지가 속한 리뷰 ID.';
COMMENT ON COLUMN p_review_image.image_url IS '이미지 URL.';
COMMENT ON COLUMN p_review_image.display_order IS '0부터 시작하는 노출 순서.';
COMMENT ON COLUMN p_review_image.created_at IS '리뷰 이미지 생성 시각.';
COMMENT ON COLUMN p_review_image.updated_at IS '리뷰 이미지 최종 수정 시각.';
COMMENT ON COLUMN p_review_image.deleted_at IS '소프트 삭제 시각.';

CREATE TABLE IF NOT EXISTS p_review_audit (
    id                UUID PRIMARY KEY,
    review_id         UUID NOT NULL,
    event_type        TEXT NOT NULL CHECK (event_type IN ('REVIEW_REGISTERED', 'REVIEW_UPDATED', 'REVIEW_DELETED')),
    change_summary    TEXT,
    actor_user_id     UUID,
    recorded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata          JSONB
);

COMMENT ON TABLE p_review_audit IS '리뷰 등록/수정/삭제 이력을 기록하는 감사 로그.';
COMMENT ON COLUMN p_review_audit.id IS '리뷰 감사 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_review_audit.review_id IS '감사 이벤트와 연결된 리뷰 ID.';
COMMENT ON COLUMN p_review_audit.event_type IS 'REVIEW_REGISTERED 등 이벤트 유형.';
COMMENT ON COLUMN p_review_audit.change_summary IS '변경 사항 요약 설명.';
COMMENT ON COLUMN p_review_audit.actor_user_id IS '이벤트를 수행한 사용자 UUID.';
COMMENT ON COLUMN p_review_audit.recorded_at IS '감사 이벤트 기록 시각.';
COMMENT ON COLUMN p_review_audit.metadata IS '추가 정보를 담은 JSON 메타데이터.';

-- ========================================
-- Stock Reservation Log (재고 예약 로그)
-- ========================================

CREATE TABLE IF NOT EXISTS p_stock_reservation_log (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id    TEXT NOT NULL UNIQUE,
    order_id          UUID NOT NULL,
    store_id          UUID NOT NULL,
    products          JSONB NOT NULL,
    status            TEXT NOT NULL CHECK (status IN ('RESERVED', 'CONFIRMED', 'RELEASED', 'EXPIRED')),
    reserved_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at        TIMESTAMPTZ NOT NULL,
    confirmed_at      TIMESTAMPTZ,
    released_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE p_stock_reservation_log IS 'Redis 재고 예약 내역을 DB에 백업 (모니터링 및 복구용)';
COMMENT ON COLUMN p_stock_reservation_log.id IS '재고 예약 로그의 UUID 기본 키.';
COMMENT ON COLUMN p_stock_reservation_log.reservation_id IS 'Redis에 저장된 예약 ID (UNIQUE).';
COMMENT ON COLUMN p_stock_reservation_log.order_id IS '예약과 연결된 주문 ID.';
COMMENT ON COLUMN p_stock_reservation_log.store_id IS '상점 ID.';
COMMENT ON COLUMN p_stock_reservation_log.products IS '예약된 상품 목록 JSON (예: [{"productId":"...", "quantity":2}])';
COMMENT ON COLUMN p_stock_reservation_log.status IS '예약 상태 (RESERVED: 예약됨, CONFIRMED: 확정됨, RELEASED: 해제됨, EXPIRED: 만료됨)';
COMMENT ON COLUMN p_stock_reservation_log.reserved_at IS '재고 예약 시각.';
COMMENT ON COLUMN p_stock_reservation_log.expires_at IS '예약 만료 예정 시각.';
COMMENT ON COLUMN p_stock_reservation_log.confirmed_at IS '예약 확정 시각.';
COMMENT ON COLUMN p_stock_reservation_log.released_at IS '예약 해제 시각.';
COMMENT ON COLUMN p_stock_reservation_log.created_at IS '레코드 생성 시각.';
COMMENT ON COLUMN p_stock_reservation_log.updated_at IS '레코드 최종 수정 시각.';

-- AI Prompt Audit (for tracking AI API requests and responses)
CREATE TABLE IF NOT EXISTS p_ai_prompt_audit (
    id                UUID PRIMARY KEY,
    user_id           UUID,
    prompt            TEXT NOT NULL,
    response          TEXT,
    model             TEXT,
    success           BOOLEAN NOT NULL DEFAULT FALSE,
    error_message     TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata          JSONB
);

COMMENT ON TABLE p_ai_prompt_audit IS 'AI API 요청 질문과 응답을 모두 저장하는 감사 로그.';
COMMENT ON COLUMN p_ai_prompt_audit.id IS 'AI 프롬프트 감사 레코드의 UUID 기본 키.';
COMMENT ON COLUMN p_ai_prompt_audit.user_id IS 'AI 요청을 수행한 사용자 UUID (선택).';
COMMENT ON COLUMN p_ai_prompt_audit.prompt IS 'AI에게 보낸 질문/프롬프트.';
COMMENT ON COLUMN p_ai_prompt_audit.response IS 'AI의 응답 텍스트.';
COMMENT ON COLUMN p_ai_prompt_audit.model IS '사용한 AI 모델 이름 (예: gemini-2.0-flash-exp).';
COMMENT ON COLUMN p_ai_prompt_audit.success IS 'AI 요청 성공 여부.';
COMMENT ON COLUMN p_ai_prompt_audit.error_message IS '실패 시 에러 메시지.';
COMMENT ON COLUMN p_ai_prompt_audit.created_at IS '감사 이벤트 기록 시각.';
COMMENT ON COLUMN p_ai_prompt_audit.metadata IS '추가 정보를 담은 JSON 메타데이터.';

-- Helpful indexes for query patterns (no foreign keys per requirements)
CREATE INDEX IF NOT EXISTS idx_p_user_role ON p_user (role);
CREATE INDEX IF NOT EXISTS idx_p_user_deleted_at ON p_user (deleted_at);
CREATE UNIQUE INDEX IF NOT EXISTS idx_p_user_address_default ON p_user_address (user_id) WHERE is_default;

CREATE INDEX IF NOT EXISTS idx_p_store_owner ON p_store (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_p_store_hidden ON p_store (hidden_at);

CREATE INDEX IF NOT EXISTS idx_p_product_category_parent ON p_product_category (parent_category_id);
CREATE INDEX IF NOT EXISTS idx_p_product_store ON p_product (store_id);
CREATE INDEX IF NOT EXISTS idx_p_product_category ON p_product (category_id);
CREATE INDEX IF NOT EXISTS idx_p_product_status ON p_product (status);
CREATE INDEX IF NOT EXISTS idx_p_product_hidden ON p_product (hidden_at);
CREATE INDEX IF NOT EXISTS idx_p_product_name_trgm ON p_product USING GIN (product_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_p_product_store_name_trgm ON p_product USING GIN (store_name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_p_order_user ON p_order (user_id);
CREATE INDEX IF NOT EXISTS idx_p_order_deleted_at ON p_order (deleted_at);

CREATE INDEX IF NOT EXISTS idx_p_order_item_order ON p_order_item (order_id);
CREATE INDEX IF NOT EXISTS idx_p_order_item_product ON p_order_item (product_id);

CREATE INDEX IF NOT EXISTS idx_p_payment_order ON p_payment (order_id);
CREATE INDEX IF NOT EXISTS idx_p_payment_method ON p_payment (method);

CREATE INDEX IF NOT EXISTS idx_p_review_product ON p_review (product_id);
CREATE INDEX IF NOT EXISTS idx_p_review_user ON p_review (user_id);

CREATE INDEX IF NOT EXISTS idx_p_ai_prompt_audit_user ON p_ai_prompt_audit (user_id);
CREATE INDEX IF NOT EXISTS idx_p_ai_prompt_audit_created_at ON p_ai_prompt_audit (created_at);

CREATE INDEX IF NOT EXISTS idx_stock_reservation_order ON p_stock_reservation_log(order_id);
CREATE INDEX IF NOT EXISTS idx_stock_reservation_expires ON p_stock_reservation_log(expires_at) WHERE status = 'RESERVED';
