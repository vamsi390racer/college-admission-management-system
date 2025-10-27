-- --------------------------------------------------------
-- College Admission Management System
-- Database: MySQL
-- --------------------------------------------------------

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS CollegeAdmissions;
USE CollegeAdmissions;

-- 2. Create Students Table (To store personal details)
CREATE TABLE Students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    -- Merit-related scores (Example: Based on a standardized test or final grade)
    score_math INT NOT NULL,
    score_physics INT NOT NULL,
    score_chemistry INT NOT NULL
);

-- 3. Create Courses Table (Available courses)
CREATE TABLE Courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100) UNIQUE NOT NULL,
    required_score INT NOT NULL, -- Minimum score required for merit calculation
    max_seats INT NOT NULL,
    current_enrolled INT DEFAULT 0
);

-- Initial Course Data
INSERT INTO Courses (course_name, required_score, max_seats) VALUES
('Computer Science', 85, 120),
('Mechanical Engineering', 75, 90),
('Electrical Engineering', 80, 100);

-- 4. Create Applications Table (Student application for a course)
CREATE TABLE Applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    merit_score DECIMAL(5, 2), -- Calculated score for ranking
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (student_id) REFERENCES Students(student_id),
    FOREIGN KEY (course_id) REFERENCES Courses(course_id),
    UNIQUE KEY (student_id, course_id) -- Prevent duplicate applications for the same course
);
