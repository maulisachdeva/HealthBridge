package com.healthbridge.service;
import com.healthbridge.dao.AppointmentDAO;
import com.healthbridge.model.Appointment;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
public class QueueManager {
    private final AppointmentDAO dao;
    public QueueManager(){this(new AppointmentDAO());}
    public QueueManager(AppointmentDAO dao){this.dao=dao;}
    public List<Appointment> getWaitingQueue(int doctorId,LocalDate date) throws SQLException {
        List<Appointment> queue=dao.getWaitingQueue(doctorId,date);
        queue.sort(Comparator.comparingInt((Appointment a)->"EMERGENCY".equals(a.getPriority())?0:1)
            .thenComparingInt(a->"SCHEDULED".equals(a.getAppointmentType())?0:1)
            .thenComparingInt(Appointment::getTokenNumber));
        return queue;
    }
    public Appointment callNextPatient(int doctorId,LocalDate date) throws SQLException{return dao.callNextPatient(doctorId,date);}
    public boolean completeAppointment(int id) throws SQLException{return dao.completeAppointment(id);}
    public boolean cancelAppointment(int id) throws SQLException{return dao.cancelAppointment(id);}
}
