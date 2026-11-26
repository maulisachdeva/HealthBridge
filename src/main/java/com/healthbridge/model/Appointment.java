package com.healthbridge.model;
import java.time.LocalDate;
import java.time.LocalTime;
public class Appointment {
    private int appointmentId, patientId, doctorId, tokenNumber;
    private String appointmentType, priority, status, reason, patientName, doctorName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    public Appointment() {}
    public Appointment(int appointmentId,int patientId,int doctorId,String appointmentType,LocalDate appointmentDate,LocalTime appointmentTime,int tokenNumber,String priority,String status,String reason) {
        this.appointmentId=appointmentId;this.patientId=patientId;this.doctorId=doctorId;this.appointmentType=appointmentType;this.appointmentDate=appointmentDate;this.appointmentTime=appointmentTime;this.tokenNumber=tokenNumber;this.priority=priority;this.status=status;this.reason=reason;
    }
    public String getPatientName(){return patientName;}
    public void setPatientName(String v){patientName=v;}
    public String getDoctorName(){return doctorName;}
    public void setDoctorName(String v){doctorName=v;}
    public int getAppointmentId(){return appointmentId;}
    public void setAppointmentId(int v){appointmentId=v;}
    public int getPatientId(){return patientId;}
    public void setPatientId(int v){patientId=v;}
    public int getDoctorId(){return doctorId;}
    public void setDoctorId(int v){doctorId=v;}
    public int getTokenNumber(){return tokenNumber;}
    public void setTokenNumber(int v){tokenNumber=v;}
    public String getAppointmentType(){return appointmentType;}
    public void setAppointmentType(String v){appointmentType=v;}
    public String getPriority(){return priority;}
    public void setPriority(String v){priority=v;}
    public String getStatus(){return status;}
    public void setStatus(String v){status=v;}
    public String getReason(){return reason;}
    public void setReason(String v){reason=v;}
    public LocalDate getAppointmentDate(){return appointmentDate;}
    public void setAppointmentDate(LocalDate v){appointmentDate=v;}
    public LocalTime getAppointmentTime(){return appointmentTime;}
    public void setAppointmentTime(LocalTime v){appointmentTime=v;}
}
