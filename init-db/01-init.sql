-- Database initialization script for PostgreSQL
-- This script runs when the PostgreSQL container starts for the first time

-- Create the application user if not exists
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'userapp') THEN

      CREATE ROLE userapp LOGIN PASSWORD 'userapp123';
   END IF;
END
$do$;

-- Grant necessary privileges
GRANT CONNECT ON DATABASE usermanagement_dev TO userapp;
GRANT USAGE ON SCHEMA public TO userapp;
GRANT CREATE ON SCHEMA public TO userapp;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO userapp;

ALTER DEFAULT PRIVILEGES IN SCHEMA public 
GRANT USAGE, SELECT ON SEQUENCES TO userapp;

-- Optional: Create indexes for better performance (will be created by Hibernate too)
-- These will be ignored if tables don't exist yet

-- Note: Hibernate will create the actual tables when the application starts
-- This script just ensures the database and user are properly configured