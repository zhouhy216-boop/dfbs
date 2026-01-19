-- V0004__masterdata_init.sql
-- 主数据：customer / contract / product / machine / iccid
-- 设计原则（3.18 冻结）：
-- - 只用“业务主键”互相引用：contract.customer_code / machine.contract_no / machine.product_code / iccid.machine_sn
-- - 软删除：deleted_at 不为空表示已删除（业务主键仍保持唯一，避免历史混乱）
-- - created_at / updated_at：基础审计时间戳（更细的审计日志以后走平台 audit 模块）

-- 1) customer
create table if not exists md_customer (
    id uuid primary key,
    customer_code varchar(64) not null,
    name varchar(200) not null,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz null
);

create unique index if not exists uk_md_customer_customer_code on md_customer(customer_code);

-- 2) contract
create table if not exists md_contract (
    id uuid primary key,
    contract_no varchar(64) not null,
    customer_code varchar(64) not null,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz null
);

create unique index if not exists uk_md_contract_contract_no on md_contract(contract_no);
create index if not exists ix_md_contract_customer_code on md_contract(customer_code);

alter table md_contract
    add constraint fk_md_contract_customer_code
    foreign key (customer_code) references md_customer(customer_code);

-- 3) product
create table if not exists md_product (
    id uuid primary key,
    product_code varchar(64) not null,
    name varchar(200) not null,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz null
);

create unique index if not exists uk_md_product_product_code on md_product(product_code);

-- 4) machine
create table if not exists md_machine (
    id uuid primary key,
    machine_sn varchar(64) not null,
    contract_no varchar(64) not null,
    product_code varchar(64) not null,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz null
);

create unique index if not exists uk_md_machine_machine_sn on md_machine(machine_sn);
create index if not exists ix_md_machine_contract_no on md_machine(contract_no);
create index if not exists ix_md_machine_product_code on md_machine(product_code);

alter table md_machine
    add constraint fk_md_machine_contract_no
    foreign key (contract_no) references md_contract(contract_no);

alter table md_machine
    add constraint fk_md_machine_product_code
    foreign key (product_code) references md_product(product_code);

-- 5) iccid
create table if not exists md_iccid (
    id uuid primary key,
    iccid_no varchar(32) not null,
    machine_sn varchar(64) null, -- 允许解绑/未绑定
    status varchar(32) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz null
);

create unique index if not exists uk_md_iccid_iccid_no on md_iccid(iccid_no);
create index if not exists ix_md_iccid_machine_sn on md_iccid(machine_sn);

alter table md_iccid
    add constraint fk_md_iccid_machine_sn
    foreign key (machine_sn) references md_machine(machine_sn);
