-- DICT-260303-001-01.b-fix1: Align dict_type.type with Hibernate (VARCHAR not CHAR) to fix schema validation.

ALTER TABLE dict_type ALTER COLUMN type TYPE VARCHAR(1) USING type::varchar(1);
