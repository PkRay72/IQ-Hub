-- ===================================================================
-- AI Based Smart Test & Assignment System - Database Schema
-- Run this whole file in MySQL Workbench / CLI before running the app
-- ===================================================================

CREATE DATABASE IF NOT EXISTS smart_test_system;
USE smart_test_system;

-- ---------------------------------------------------------------
-- USERS  (Teacher / Student / HOD)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          ENUM('TEACHER','STUDENT','HOD') NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------
-- QUESTIONS  (added by Teacher -> reviewed by HOD)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS questions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    subject         VARCHAR(100) NOT NULL,
    question_text   TEXT NOT NULL,
    option_a        VARCHAR(255) NOT NULL,
    option_b        VARCHAR(255) NOT NULL,
    option_c        VARCHAR(255) NOT NULL,
    option_d        VARCHAR(255) NOT NULL,
    correct_option  CHAR(1) NOT NULL,              -- A / B / C / D
    difficulty      ENUM('EASY','MEDIUM','HARD') NOT NULL DEFAULT 'MEDIUM',
    status          ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_by      INT NOT NULL,                  -- teacher user id
    reviewed_by     INT NULL,                       -- HOD user id
    review_remarks  VARCHAR(500) NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at     TIMESTAMP NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

-- ---------------------------------------------------------------
-- TEST RESULTS  (Student attempts)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS test_results (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    subject       VARCHAR(100) NOT NULL,
    total_marks   INT NOT NULL,
    scored_marks  INT NOT NULL,
    rating        INT NOT NULL,           -- 1 to 5 stars
    attempted_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- ---------------------------------------------------------------
-- TEST ANSWERS (per-question detail for a result, used for "Show Answers")
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS test_answers (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    result_id        INT NOT NULL,
    question_id      INT NOT NULL,
    selected_option  CHAR(1) NULL,
    is_correct       TINYINT(1) NOT NULL,
    FOREIGN KEY (result_id) REFERENCES test_results(id),
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- ---------------------------------------------------------------
-- SEED DATA  (default login accounts, password = "password123" for all)
-- SHA2(...,256) in MySQL produces the same lowercase-hex format that the
-- Java app's PasswordUtil (SHA-256) produces, so these logins work directly.
-- ---------------------------------------------------------------
INSERT INTO users (username, password_hash, full_name, role) VALUES
('hod1',     SHA2('password123',256), 'Dr. HOD Sharma',   'HOD'),
('teacher1', SHA2('password123',256), 'Mr. Rakesh Verma', 'TEACHER'),
('student1', SHA2('password123',256), 'Amit Kumar',       'STUDENT');

-- You can also just click "Register" inside the app to create fresh
-- Teacher / Student accounts (HOD accounts must be inserted via SQL
-- for security, using the same SHA2(...,256) pattern above).
