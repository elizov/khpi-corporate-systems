DELETE FROM users;
INSERT INTO users (id, username, email, password, role, age, phone, address, city, postal_code) VALUES
(1, 'admin', 'admin@example.com', '$2a$10$IP6648hHsiNzInzr4H.bjOt.3Znt/vyeaHQ4ajxlIlHqoY1dPWV.y', 'ADMIN', 30, '+10000000000', 'Admin street', 'Admin City', '00001'),
(2, 'user', 'user@example.com', '$2a$10$IP6648hHsiNzInzr4H.bjOt.3Znt/vyeaHQ4ajxlIlHqoY1dPWV.y', 'USER', 25, '+20000000000', 'User street', 'User City', '00002');
-- passwords: pass
