-- V0005__quote_item.sql
-- 报价明细（QuoteItem）表结构
-- 设计原则：
-- - quote_version_id 外键关联 quote_version.id
-- - item_type: 明细类型（PRODUCT / SERVICE / EXPENSE）
-- - source_type: 来源类型（MANUAL / ENTROST / ...）
-- - source_id: 来源ID（可为空，表示人工录入）
-- - amount = unit_price * quantity（总价 = 单价 * 数量）

CREATE TABLE IF NOT EXISTS quote_item (
    id BIGSERIAL PRIMARY KEY,
    quote_version_id UUID NOT NULL,
    item_type VARCHAR(32) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_id VARCHAR(128),
    item_name VARCHAR(500) NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    quantity NUMERIC(19, 2) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    remark VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS ix_quote_item_quote_version_id ON quote_item(quote_version_id);

ALTER TABLE quote_item
    ADD CONSTRAINT fk_quote_item_quote_version_id
    FOREIGN KEY (quote_version_id) REFERENCES quote_version(id);
