DELETE FROM users;
DELETE FROM products;

INSERT INTO users (username, email, password, age) VALUES
('user', 'user@example.com', 'pass', 30),
('admin', 'admin@example.com', 'pass', 30),
('john', 'john.doe@example.com', 'pass', 25),
('anna', 'anna.smith@example.com', 'pass', 28),
('mike', 'mike.ross@example.com', 'pass', 32),
('sara', 'sara.connor@example.com', 'pass', 35);


-- Electronics
INSERT INTO products (name, category, price, description) VALUES
('Lenovo ThinkPad X1 Carbon', 'Electronics', 38500.00, 'Lightweight business laptop with Intel i7 processor'),
('Apple iPhone 15', 'Electronics', 42999.00, 'Latest generation iPhone with A17 Bionic chip'),
('Samsung Galaxy S23', 'Electronics', 38999.00, 'Flagship Android smartphone with triple camera'),
('Sony WH-1000XM5', 'Electronics', 12500.00, 'Wireless noise-cancelling headphones'),
('Xiaomi Smartwatch 8 Pro', 'Electronics', 5800.00, 'Smartwatch with health monitoring features');

-- Fashion
INSERT INTO products (name, category, price, description) VALUES
('Nike Air Max Sneakers', 'Fashion', 4200.00, 'Comfortable sneakers with air cushion sole'),
('Adidas Hoodie', 'Fashion', 2100.00, 'Cotton hoodie for everyday wear'),
('Leather Jacket', 'Fashion', 8800.00, 'Genuine leather biker-style jacket'),
('Ray-Ban Aviator Sunglasses', 'Fashion', 4600.00, 'Classic aviator sunglasses with UV protection'),
('Casio G-Shock Watch', 'Fashion', 3100.00, 'Durable sport watch with water resistance');

-- Home & Living
INSERT INTO products (name, category, price, description) VALUES
('Ikea Markus Chair', 'Home & Living', 4500.00, 'Ergonomic office chair with adjustable height'),
('Wooden Dining Table', 'Home & Living', 11200.00, 'Solid oak dining table for 6 people'),
('Philips Blender HR3652', 'Home & Living', 2400.00, 'Powerful blender for smoothies and sauces'),
('Samsung 55" QLED TV', 'Home & Living', 27500.00, '4K smart TV with vivid colors and HDR'),
('Tefal Electric Kettle', 'Home & Living', 1150.00, '1.7L stainless steel kettle with auto shut-off');

-- Books
INSERT INTO products (name, category, price, description) VALUES
('The Great Gatsby', 'Books', 350.00, 'Classic novel by F. Scott Fitzgerald'),
('Clean Code', 'Books', 980.00, 'Programming book by Robert C. Martin'),
('Design Patterns', 'Books', 1200.00, 'Seminal book on software architecture'),
('Harry Potter Box Set', 'Books', 2800.00, 'Complete collection of Harry Potter novels'),
('Spring in Action', 'Books', 1100.00, 'Practical guide to Spring Framework development');
