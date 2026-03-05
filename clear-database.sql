-- Database Reset Script for PetSave API
-- This will clear all data while preserving table structure

-- Disable foreign key constraints temporarily
SET session_replication_role = replica;

-- Clear data in dependency order (child tables first)
DELETE FROM donations;
DELETE FROM adoptions;
DELETE FROM users;

-- Reset auto-increment sequences
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE adoptions_id_seq RESTART WITH 1;
ALTER SEQUENCE donations_id_seq RESTART WITH 1;

-- Re-enable foreign key constraints
SET session_replication_role = DEFAULT;

-- Verify the reset
SELECT 'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'adoptions' as table_name, COUNT(*) as record_count FROM adoptions
UNION ALL
SELECT 'donations' as table_name, COUNT(*) as record_count FROM donations;
