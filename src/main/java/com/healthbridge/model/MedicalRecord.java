package com.healthbridge.model;

import java.sql.Date;

public class MedicalRecord {

    private int recordId;
    private int patientId;
    private int doctorId;
    private int appointmentId;

    private String symptoms;
    private String diagnosis;
    private String treatment;

    private Date recordDate;

    // Default constructor
    public MedicalRecord() {
    }

    // Parameterized constructor
    public MedicalRecord(int recordId, int patientId,
                         int doctorId, int appointmentId,
                         String symptoms, String diagnosis,
                         String treatment, Date recordDate) {

        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.recordDate = recordDate;
    }

    // Getters and Setters

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }
}