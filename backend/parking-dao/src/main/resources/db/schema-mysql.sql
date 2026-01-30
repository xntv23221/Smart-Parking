-- Drop Tables (Order matters for Foreign Keys)
DROP TABLE IF EXISTS parking_records;
DROP TABLE IF EXISTS pricing_rules;
DROP TABLE IF EXISTS monthly_cards;
DROP TABLE IF EXISTS parking_spots;
DROP TABLE IF EXISTS parking_lots;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS wallet_log;
DROP TABLE IF EXISTS user_wallet;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS audit_requests;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS managers;
DROP TABLE IF EXISTS system_admins;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS audit_log;

-- Legacy Drops (Cleanup)
DROP TABLE IF EXISTS fee_rule;
DROP TABLE IF EXISTS parking_order;
DROP TABLE IF EXISTS parking_space;
DROP TABLE IF EXISTS sys_user;

-- User Subsystem
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash CHAR(64) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100),
    nickname VARCHAR(50),
    avatar_url VARCHAR(255),
    balance DECIMAL(10,2) DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '0=Disabled, 1=Normal'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    sender_role VARCHAR(20) COMMENT 'admin, manager, system',
    receiver_id BIGINT NOT NULL,
    receiver_role VARCHAR(20) COMMENT 'user, manager',
    content TEXT,
    type VARCHAR(20) COMMENT 'system, parking, private',
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_requests (
    request_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) COMMENT 'phone_change',
    old_value VARCHAR(100),
    new_value VARCHAR(100),
    status TINYINT DEFAULT 0 COMMENT '0=Pending, 1=Approved, 2=Rejected',
    reason VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    handled_at DATETIME,
    handler_id BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plate_number VARCHAR(20) UNIQUE NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(50),
    color VARCHAR(20),
    vehicle_type TINYINT COMMENT '1=Small, 2=Large, 3=New Energy',
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_wallet (
    wallet_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00,
    version INT DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wallet_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type TINYINT COMMENT '1=Recharge, 2=Payment, 3=Refund',
    balance_after DECIMAL(10,2),
    order_id BIGINT,
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_wallet FOREIGN KEY (wallet_id) REFERENCES user_wallet(wallet_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS invoices (
    invoice_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    title VARCHAR(100),
    tax_no VARCHAR(50),
    status TINYINT DEFAULT 0 COMMENT '0=Pending, 1=Issued',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- System Admin Subsystem
CREATE TABLE IF NOT EXISTS system_admins (
    admin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash CHAR(64) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    role VARCHAR(20) COMMENT 'super, operator',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS managers (
    manager_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash CHAR(64) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1 COMMENT '0=Disabled, 1=Enabled',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Parking Lot Subsystem
CREATE TABLE IF NOT EXISTS parking_lots (
    lot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    total_spots INT DEFAULT 0,
    available_spots INT DEFAULT 0,
    manager_id BIGINT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    status TINYINT DEFAULT 1 COMMENT '0=Closed, 1=Open',
    type TINYINT DEFAULT 0 COMMENT '0=Public, 1=Commercial',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lots_manager FOREIGN KEY (manager_id) REFERENCES managers(manager_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS parking_spots (
    spot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lot_id BIGINT NOT NULL,
    spot_number VARCHAR(20) NOT NULL,
    spot_type TINYINT DEFAULT 1 COMMENT '1=Normal, 2=Disabled, 3=Charging',
    is_occupied BOOLEAN DEFAULT FALSE,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_spots_lot FOREIGN KEY (lot_id) REFERENCES parking_lots(lot_id) ON DELETE CASCADE,
    UNIQUE KEY idx_lot_spot_number (lot_id, spot_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS monthly_cards (
    card_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    lot_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    plate_number VARCHAR(20),
    status TINYINT DEFAULT 1 COMMENT '1=Active, 0=Expired',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_cards_lot FOREIGN KEY (lot_id) REFERENCES parking_lots(lot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pricing_rules (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lot_id BIGINT NOT NULL,
    vehicle_type TINYINT,
    base_duration INT DEFAULT 0 COMMENT 'Free minutes',
    base_price DECIMAL(6,2) DEFAULT 0.00 COMMENT 'First hour price',
    extra_price_per_hour DECIMAL(6,2) DEFAULT 0.00,
    daily_cap DECIMAL(6,2),
    effective_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_rules_lot FOREIGN KEY (lot_id) REFERENCES parking_lots(lot_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS parking_records (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    lot_id BIGINT NOT NULL,
    spot_id BIGINT NOT NULL,
    entry_time DATETIME,
    exit_time DATETIME,
    duration_minutes INT,
    amount DECIMAL(8,2) DEFAULT 0.00,
    paid_amount DECIMAL(8,2) DEFAULT 0.00,
    payment_status TINYINT DEFAULT 0 COMMENT '0=Unpaid, 1=Paid, 2=Partial',
    status TINYINT DEFAULT 0 COMMENT '0=Booked, 1=Parked, 2=Completed, 3=Cancelled',
    payment_method VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_records_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_records_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    CONSTRAINT fk_records_lot FOREIGN KEY (lot_id) REFERENCES parking_lots(lot_id),
    CONSTRAINT fk_records_spot FOREIGN KEY (spot_id) REFERENCES parking_spots(spot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes
CREATE INDEX idx_records_lot_entry ON parking_records(lot_id, entry_time);

-- Initial Data
INSERT INTO system_admins (username, password_hash, role) VALUES ('admin', '$2a$10$EblZqNptyYpXnRenCdTA7.Wc4M9.A3M/6y6kG1gE6.7J6.7J6.7J6', 'super'); -- password: admin
-- Data seeding is handled by DataSeeder.java

