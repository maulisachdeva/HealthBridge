package com.healthbridge.model;

import java.time.LocalDate;

public class Bill {

    private int billId;
    private int patientId;
    private int appointmentId;

    private double consultationCharge;
    private double medicineCharge;
    private double otherCharge;
    private double totalAmount;

    private String paymentStatus;
    private LocalDate billDate;

    // Default Constructor
    public Bill() {
    }

    // Parameterized Constructor
    public Bill(int patientId, int appointmentId,
                double consultationCharge,
                double medicineCharge,
                double otherCharge,
                LocalDate billDate) {

        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.consultationCharge = consultationCharge;
        this.medicineCharge = medicineCharge;
        this.otherCharge = otherCharge;

        this.totalAmount = consultationCharge
                + medicineCharge
                + otherCharge;

        this.paymentStatus = "PENDING";
        this.billDate = billDate;
    }

    // Getters and Setters

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public double getConsultationCharge() {
        return consultationCharge;
    }

    public void setConsultationCharge(double consultationCharge) {
        this.consultationCharge = consultationCharge;
    }

    public double getMedicineCharge() {
        return medicineCharge;
    }

    public void setMedicineCharge(double medicineCharge) {
        this.medicineCharge = medicineCharge;
    }

    public double getOtherCharge() {
        return otherCharge;
    }

    public void setOtherCharge(double otherCharge) {
        this.otherCharge = otherCharge;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }
}