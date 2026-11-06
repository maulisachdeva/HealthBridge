

-- =========================================
-- HEALTHBRIDGE
-- Smart Clinic Management System
-- Database Schema
-- =========================================

CREATE DATABASE IF NOT EXISTS healthbridge;

USE healthbridge;

-- =========================================
-- PATIENT TABLE
-- =========================================

CREATE TABLE IF NOT EXISTS patients (
                                        patient_id INT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY unique_patient (name, phone)
    );

-- =========================================
-- DOCTOR TABLE
-- =========================================

CREATE TABLE IF NOT EXISTS doctors (
                                       doctor_id INT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY unique_doctor_phone (phone),
    UNIQUE KEY unique_doctor_email (email)
    );
