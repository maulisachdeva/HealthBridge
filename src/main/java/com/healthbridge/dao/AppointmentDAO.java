package com.healthbridge.dao;
import com.healthbridge.model.Appointment;
import com.healthbridge.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class AppointmentDAO {
    public boolean addAppointment(Appointment a) throws SQLException {
        if(a==null||a.getPatientId()<=0||a.getDoctorId()<=0||a.getAppointmentDate()==null)throw new IllegalArgumentException("Patient, doctor and date are required");
        if(!"SCHEDULED".equals(a.getAppointmentType())&&!"WALK_IN".equals(a.getAppointmentType()))throw new IllegalArgumentException("Invalid appointment type");
        if("SCHEDULED".equals(a.getAppointmentType())&&a.getAppointmentTime()==null)throw new IllegalArgumentException("Scheduled appointments require a time");
        if(!"NORMAL".equals(a.getPriority())&&!"EMERGENCY".equals(a.getPriority()))throw new IllegalArgumentException("Invalid priority");
        String insert="INSERT INTO appointments (patient_id,doctor_id,appointment_type,appointment_date,appointment_time,token_number,priority,status,reason) VALUES (?,?,?,?,?,?,?,?,?)";
        for(int attempt=0;attempt<5;attempt++){
            try(Connection con=DBConnection.getConnection()){
                con.setAutoCommit(false);
                try{
                    try(PreparedStatement lock=con.prepareStatement("SELECT doctor_id FROM doctors WHERE doctor_id=? FOR UPDATE")){
                        lock.setInt(1,a.getDoctorId());
                        try(ResultSet rs=lock.executeQuery()){if(!rs.next())throw new SQLException("Doctor not found");}
                    }
                    int token;
                    try(PreparedStatement ps=con.prepareStatement("SELECT COALESCE(MAX(token_number),0)+1 FROM appointments WHERE doctor_id=? AND appointment_date=?")){
                        ps.setInt(1,a.getDoctorId());ps.setDate(2,Date.valueOf(a.getAppointmentDate()));
                        try(ResultSet rs=ps.executeQuery()){rs.next();token=rs.getInt(1);}
                    }
                    int id;
                    try(PreparedStatement ps=con.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS)){
                        ps.setInt(1,a.getPatientId());ps.setInt(2,a.getDoctorId());ps.setString(3,a.getAppointmentType());ps.setDate(4,Date.valueOf(a.getAppointmentDate()));
                        if(a.getAppointmentTime()==null)ps.setNull(5,Types.TIME);else ps.setTime(5,Time.valueOf(a.getAppointmentTime()));
                        ps.setInt(6,token);ps.setString(7,a.getPriority());ps.setString(8,"WAITING");ps.setString(9,a.getReason());
                        if(ps.executeUpdate()!=1)throw new SQLException("Booking failed");
                        try(ResultSet keys=ps.getGeneratedKeys()){if(!keys.next())throw new SQLException("Missing appointment ID");id=keys.getInt(1);}
                    }
                    con.commit();a.setAppointmentId(id);a.setTokenNumber(token);a.setStatus("WAITING");return true;
                }catch(SQLException ex){con.rollback();if(ex.getErrorCode()==1062&&attempt<4)continue;throw ex;}
            }
        }
        throw new SQLException("Unable to generate a unique token");
    }
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> result=new ArrayList<>();
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement("SELECT * FROM appointments ORDER BY appointment_date DESC,doctor_id,token_number");ResultSet rs=ps.executeQuery()){
            while(rs.next())result.add(map(rs));
        }
        return result;
    }
    public Appointment getAppointmentById(int id) throws SQLException {
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement("SELECT * FROM appointments WHERE appointment_id=?")){
            ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?map(rs):null;}
        }
    }
    public List<Appointment> getWaitingQueue(int doctorId,LocalDate date) throws SQLException {
        List<Appointment> result=new ArrayList<>();
        String sql="SELECT * FROM appointments WHERE doctor_id=? AND appointment_date=? AND status='WAITING' ORDER BY CASE WHEN priority='EMERGENCY' THEN 0 ELSE 1 END,CASE WHEN appointment_type='SCHEDULED' THEN 0 ELSE 1 END,token_number";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,doctorId);ps.setDate(2,Date.valueOf(date));
            try(ResultSet rs=ps.executeQuery()){while(rs.next())result.add(map(rs));}
        }
        return result;
    }
    public boolean updateAppointmentStatus(int id,String status) throws SQLException {
        if("COMPLETED".equals(status))return completeAppointment(id);
        if("CANCELLED".equals(status))return cancelAppointment(id);
        if(!"IN_PROGRESS".equals(status))throw new IllegalArgumentException("Invalid status transition");
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement("UPDATE appointments SET status='IN_PROGRESS' WHERE appointment_id=? AND status='WAITING'")){
            ps.setInt(1,id);return ps.executeUpdate()==1;
        }
    }
    public Appointment callNextPatient(int doctorId,LocalDate date) throws SQLException {
        try(Connection con=DBConnection.getConnection()){
            con.setAutoCommit(false);
            try{
                try(PreparedStatement lock=con.prepareStatement("SELECT doctor_id FROM doctors WHERE doctor_id=? FOR UPDATE")){
                    lock.setInt(1,doctorId);try(ResultSet rs=lock.executeQuery()){if(!rs.next())throw new SQLException("Doctor not found");}
                }
                try(PreparedStatement active=con.prepareStatement("SELECT appointment_id FROM appointments WHERE doctor_id=? AND appointment_date=? AND status='IN_PROGRESS' LIMIT 1")){
                    active.setInt(1,doctorId);active.setDate(2,Date.valueOf(date));try(ResultSet rs=active.executeQuery()){if(rs.next())throw new SQLException("Doctor already has a patient in progress");}
                }
                Appointment next=null;
                String sql="SELECT * FROM appointments WHERE doctor_id=? AND appointment_date=? AND status='WAITING' ORDER BY CASE WHEN priority='EMERGENCY' THEN 0 ELSE 1 END,CASE WHEN appointment_type='SCHEDULED' THEN 0 ELSE 1 END,token_number LIMIT 1";
                try(PreparedStatement ps=con.prepareStatement(sql)){
                    ps.setInt(1,doctorId);ps.setDate(2,Date.valueOf(date));try(ResultSet rs=ps.executeQuery()){if(rs.next())next=map(rs);}
                }
                if(next==null){con.commit();return null;}
                try(PreparedStatement ps=con.prepareStatement("UPDATE appointments SET status='IN_PROGRESS' WHERE appointment_id=? AND status='WAITING'")){
                    ps.setInt(1,next.getAppointmentId());if(ps.executeUpdate()!=1)throw new SQLException("Unable to call patient");
                }
                con.commit();next.setStatus("IN_PROGRESS");return next;
            }catch(SQLException ex){con.rollback();throw ex;}
        }
    }
    public boolean completeAppointment(int id) throws SQLException{return changeStatus(id,"IN_PROGRESS","COMPLETED");}
    public boolean cancelAppointment(int id) throws SQLException{return changeStatus(id,"WAITING","CANCELLED");}
    private boolean changeStatus(int id,String from,String to) throws SQLException {
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement("UPDATE appointments SET status=? WHERE appointment_id=? AND status=?")){
            ps.setString(1,to);ps.setInt(2,id);ps.setString(3,from);return ps.executeUpdate()==1;
        }
    }
    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a=new Appointment();a.setAppointmentId(rs.getInt("appointment_id"));a.setPatientId(rs.getInt("patient_id"));a.setDoctorId(rs.getInt("doctor_id"));a.setAppointmentType(rs.getString("appointment_type"));
        Date d=rs.getDate("appointment_date");a.setAppointmentDate(d==null?null:d.toLocalDate());Time t=rs.getTime("appointment_time");a.setAppointmentTime(t==null?null:t.toLocalTime());
        a.setTokenNumber(rs.getInt("token_number"));a.setPriority(rs.getString("priority"));a.setStatus(rs.getString("status"));a.setReason(rs.getString("reason"));return a;
    }
}
