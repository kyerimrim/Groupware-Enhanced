-- Run separately before 01_schema.sql.
-- Expected: LOGIN_USER=GROUPWARE, CURRENT_SCHEMA=GROUPWARE, DB_NAME=XEPDB1.
-- Do not run the creation script if any value differs.
SELECT USER AS LOGIN_USER,
       SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS CURRENT_SCHEMA,
       SYS_CONTEXT('USERENV', 'CON_NAME') AS DB_NAME
FROM DUAL;

-- Expected: no existing application tables in a new schema.
-- If rows exist, inspect them before running the creation script.
SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME;
