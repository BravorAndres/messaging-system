-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS messaging_db;
USE messaging_db;

-- Crear la tabla authorized_lines
CREATE TABLE authorized_lines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    line_number VARCHAR(20) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ahora ya puedes ejecutar tus inserts:
INSERT INTO authorized_lines (line_number, active) VALUES ('1234567890', true),('0987654321', true),('1111111111', true),('2222222222', true),('3333333333', true);