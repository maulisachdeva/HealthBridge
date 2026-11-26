package com.healthbridge.model;
import java.time.LocalDate;
public class Token {
    private final int number, doctorId, appointmentId;
    private final LocalDate date;
    public Token(int number,int doctorId,int appointmentId,LocalDate date){this.number=number;this.doctorId=doctorId;this.appointmentId=appointmentId;this.date=date;}
    public int getNumber(){return number;}
    public int getDoctorId(){return doctorId;}
    public int getAppointmentId(){return appointmentId;}
    public LocalDate getDate(){return date;}
    public static Token fromAppointment(Appointment a){return new Token(a.getTokenNumber(),a.getDoctorId(),a.getAppointmentId(),a.getAppointmentDate());}
    @Override public String toString(){return "Token " + number;}
}
