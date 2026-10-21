-- Existing databases only. New databases created with 01_schema.sql already include this column.
-- Run once as GROUPWARE after checking 00_check_connection.sql.
ALTER TABLE EMPLOYEES ADD (PROFILE_IMAGE CLOB);
