-- DICT-260303-001-01.b: Data Dictionary metadata type (A/B/C/D). stableKey = type_code (unchanged).

ALTER TABLE dict_type ADD COLUMN IF NOT EXISTS type CHAR(1) NOT NULL DEFAULT 'A';
ALTER TABLE dict_type ADD CONSTRAINT chk_dict_type_type CHECK (type IN ('A', 'B', 'C', 'D'));
COMMENT ON COLUMN dict_type.type IS 'Metadata type A/B/C/D for data dictionary. type_code remains stableKey (immutable after create).';
