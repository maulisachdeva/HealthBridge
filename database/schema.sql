
CREATE DATABASE IF NOT EXISTS healthbridge;
USE healthbridge;

-- =========================================
-- 1. PATIENTS
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
-- 2. DOCTORS
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

-- =========================================
-- 3. APPOINTMENTS
-- =========================================

CREATE TABLE IF NOT EXISTS appointments (
                                            appointment_id INT AUTO_INCREMENT PRIMARY KEY,
                                            patient_id INT NOT NULL,
                                            doctor_id INT NOT NULL,
                                            appointment_type VARCHAR(20) NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME,
    token_number INT NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    status VARCHAR(30) DEFAULT 'WAITING',
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointment_patient
    FOREIGN KEY (patient_id)
    REFERENCES patients(patient_id),

    CONSTRAINT fk_appointment_doctor
    FOREIGN KEY (doctor_id)
    REFERENCES doctors(doctor_id),

    CONSTRAINT unique_doctor_token
    UNIQUE (doctor_id, appointment_date, token_number)
    );

-- =========================================
-- 4. MEDICAL RECORDS
-- =========================================

CREATE TABLE IF NOT EXISTS medical_records (
                                               record_id INT AUTO_INCREMENT PRIMARY KEY,
                                               patient_id INT NOT NULL,
                                               doctor_id INT NOT NULL,
                                               appointment_id INT NOT NULL,
                                               symptoms VARCHAR(500),
    diagnosis VARCHAR(500) NOT NULL,
    treatment TEXT,
    record_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_record_patient
    FOREIGN KEY (patient_id)
    REFERENCES patients(patient_id),

    CONSTRAINT fk_record_doctor
    FOREIGN KEY (doctor_id)
    REFERENCES doctors(doctor_id),

    CONSTRAINT fk_record_appointment
    FOREIGN KEY (appointment_id)
    REFERENCES appointments(appointment_id),

    CONSTRAINT unique_appointment_record
    UNIQUE (appointment_id)
    );

-- =========================================
-- 5. PRESCRIPTIONS
-- =========================================

CREATE TABLE IF NOT EXISTS prescriptions (
                                             prescription_id INT AUTO_INCREMENT PRIMARY KEY,
                                             record_id INT NOT NULL,
                                             medicine_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration VARCHAR(100) NOT NULL,
    instructions VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prescription_record
    FOREIGN KEY (record_id)
    REFERENCES medical_records(record_id)
    );

-- =========================================
-- 6. BILLS
-- =========================================

CREATE TABLE IF NOT EXISTS bills (
                                     bill_id INT AUTO_INCREMENT PRIMARY KEY,
                                     patient_id INT NOT NULL,
                                     appointment_id INT NOT NULL,
                                     consultation_charge DECIMAL(10,2) DEFAULT 0.00,
    medicine_charge DECIMAL(10,2) DEFAULT 0.00,
    other_charge DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    bill_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bill_patient
    FOREIGN KEY (patient_id)
    REFERENCES patients(patient_id),

    CONSTRAINT fk_bill_appointment
    FOREIGN KEY (appointment_id)
    REFERENCES appointments(appointment_id),

    CONSTRAINT unique_appointment_bill
    UNIQUE (appointment_id)
    );

-- =========================================
-- 7. PAYMENTS
-- =========================================

CREATE TABLE IF NOT EXISTS payments (
                                        payment_id INT AUTO_INCREMENT PRIMARY KEY,
                                        bill_id INT NOT NULL,
                                        amount_paid DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_reference VARCHAR(100),
    payment_status VARCHAR(20) DEFAULT 'SUCCESS',

    CONSTRAINT fk_payment_bill
    FOREIGN KEY (bill_id)
    REFERENCES bills(bill_id)
    );

-- =========================================
-- 8. STAFF USERS
-- =========================================

CREATE TABLE IF NOT EXISTS staff_users (
                                           staff_id INT AUTO_INCREMENT PRIMARY KEY,
                                           full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'RECEPTIONIST',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
