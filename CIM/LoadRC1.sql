-- for manual loading:
DROP DATABASE IF EXISTS OSPRREYS;
CREATE DATABASE OSPRREYS;
USE OSPRREYS;

source rc1.SQL;


-- check tables after creating the database
show TABLES;
DESC Breaker;

-- check foreign constraints
use INFORMATION_SCHEMA;
select TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME,
REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME from KEY_COLUMN_USAGE
where TABLE_SCHEMA = "OSPRREYS" and referenced_column_name is not NULL;
