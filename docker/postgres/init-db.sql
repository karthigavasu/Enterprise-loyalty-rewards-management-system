-- PostgreSQL Database Initialization for Enterprise Loyalty & Rewards Platform
-- Independent databases for microservices data ownership

CREATE DATABASE customer_db;
CREATE DATABASE loyalty_db;
CREATE DATABASE rewards_db;
CREATE DATABASE transaction_db;

-- Grant permissions to default user
GRANT ALL PRIVILEGES ON DATABASE customer_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE loyalty_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE rewards_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE transaction_db TO postgres;
