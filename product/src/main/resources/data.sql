TRUNCATE TABLE products;

-- Electronics
INSERT INTO products (name, category, price, description, image_url) VALUES
('Lenovo ThinkPad X1 Carbon', 'Electronics', 38500.00, 'Lightweight business laptop with Intel i7 processor', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=900&q=80'),
('Apple iPhone 15', 'Electronics', 42999.00, 'Latest generation iPhone with A17 Bionic chip', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=80'),
('Samsung Galaxy S23', 'Electronics', 38999.00, 'Flagship Android smartphone with triple camera', 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=900&q=80'),
('Sony WH-1000XM5', 'Electronics', 12500.00, 'Wireless noise-cancelling headphones', 'https://images.unsplash.com/photo-1519677100203-a0e668c92439?auto=format&fit=crop&w=900&q=80'),
('Xiaomi Smartwatch 8 Pro', 'Electronics', 5800.00, 'Smartwatch with health monitoring features', 'https://images.unsplash.com/photo-1519241047957-be31d7379a5d?auto=format&fit=crop&w=900&q=80');

-- Fashion
INSERT INTO products (name, category, price, description, image_url) VALUES
('Nike Air Max Sneakers', 'Fashion', 4200.00, 'Comfortable sneakers with air cushion sole', 'https://images.unsplash.com/photo-1514986888952-8cd320577b68?auto=format&fit=crop&w=900&q=80'),
('Adidas Hoodie', 'Fashion', 2100.00, 'Cotton hoodie for everyday wear', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=900&q=80'),
('Leather Jacket', 'Fashion', 8800.00, 'Genuine leather biker-style jacket', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80'),
('Ray-Ban Aviator Sunglasses', 'Fashion', 4600.00, 'Classic aviator sunglasses with UV protection', 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=900&q=80'),
('Casio G-Shock Watch', 'Fashion', 3100.00, 'Durable sport watch with water resistance', 'https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=900&q=80');

-- Home & Living
INSERT INTO products (name, category, price, description, image_url) VALUES
('Ikea Markus Chair', 'Home & Living', 4500.00, 'Ergonomic office chair with adjustable height', 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=900&q=80'),
('Wooden Dining Table', 'Home & Living', 11200.00, 'Solid oak dining table for 6 people', 'https://images.unsplash.com/photo-1549187774-b4e9b0445b41?auto=format&fit=crop&w=900&q=80'),
('Philips Blender HR3652', 'Home & Living', 2400.00, 'Powerful blender for smoothies and sauces', 'https://images.unsplash.com/photo-1481391032119-d89fee407e44?auto=format&fit=crop&w=900&q=80'),
('Samsung 55\" QLED TV', 'Home & Living', 27500.00, '4K smart TV with vivid colors and HDR', 'https://images.unsplash.com/photo-1545239351-1141bd82e8a6?auto=format&fit=crop&w=900&q=80'),
('Tefal Electric Kettle', 'Home & Living', 1150.00, '1.7L stainless steel kettle with auto shut-off', 'https://images.unsplash.com/photo-1509460913899-515f1df34fea?auto=format&fit=crop&w=900&q=80');

-- Books
INSERT INTO products (name, category, price, description, image_url) VALUES
('The Great Gatsby', 'Books', 350.00, 'Classic novel by F. Scott Fitzgerald', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=900&q=80'),
('Clean Code', 'Books', 980.00, 'Programming book by Robert C. Martin', 'https://images.unsplash.com/photo-1523475472560-d2df97ec485c?auto=format&fit=crop&w=900&q=80'),
('Design Patterns', 'Books', 1200.00, 'Seminal book on software architecture', 'https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=900&q=80'),
('Harry Potter Box Set', 'Books', 2800.00, 'Complete collection of Harry Potter novels', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=900&q=80'),
('Spring in Action', 'Books', 1100.00, 'Practical guide to Spring Framework development', 'https://images.unsplash.com/photo-1541963463532-d68292c34b19?auto=format&fit=crop&w=900&q=80');
