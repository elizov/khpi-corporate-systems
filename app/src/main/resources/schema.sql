CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    phone VARCHAR(50),
    address VARCHAR(255),
    city VARCHAR(100),
    postal_code VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    created_at DATETIME NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    delivery_method VARCHAR(60) NOT NULL,
    payment_method VARCHAR(60) NOT NULL,
    card_last_four VARCHAR(4),
    notes VARCHAR(300),
    user_id BIGINT,
    total_quantity INT NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(36) NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
