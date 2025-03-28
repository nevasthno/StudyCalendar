CREATE DATABASE IF NOT EXISTS PeopleAndEvents;
USE PeopleAndEvents;
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    about_me TEXT,
    date_of_birth DATE,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('teacher', 'parent', 'student') NOT NULL
);
-- Таблиця подій
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    deadline DATETIME NOT NULL,
    isCompleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Таблиця завдань
CREATE TABLE IF NOT EXISTS events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    locationOrLink TEXT,
    eventType ENUM(
        'exam',
        'test',
        'school_event',
        'parents_meeting',
        'personal'
    ) NOT NULL,
    duration INT NOT NULL,
    -- У хвилинах
    startEvent DATETIME NOT NULL,
    task_id INT DEFAULT NULL,
    -- Опціональне завдання
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE
    SET NULL
);
-- Таблиця запрошеннь
CREATE TABLE IF NOT EXISTS invitations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    status ENUM('pending', 'accepted', 'declined') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);