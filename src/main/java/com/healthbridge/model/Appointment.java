package com.healthbridge.model;

import  java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    private int appointmentId;
    private int patientId;
    private int doctorId;

    private String appointmentType;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private int tokenNumber;
    private String priority;
    private String status;
    private String reason;

    public Appointment(){
    }

    public Appointment(int appointmentId, int patientId,
                       int doctorId, String appointmentType,
                       LocalDate appointmentDate, LocalTime appointmentTime,
                       int tokenNumber, String priority,
                       String status, String reason){

        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentType = appointmentType;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.tokenNumber = tokenNumber;
        this.priority = priority;
        this.status = status;
        this.reason = reason;

    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
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

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(int tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
