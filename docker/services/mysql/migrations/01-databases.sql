CREATE DATABASE IF NOT EXISTS app;

CREATE USER 'user'@'%' IDENTIFIED WITH mysql_native_password BY 'pass';

GRANT ALL PRIVILEGES ON app.* TO 'user'@'%';
