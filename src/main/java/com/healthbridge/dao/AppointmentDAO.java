
package com.healthbridge.dao;

import com.healthbridge.model.Appointment;
import com.healthbridge.util.DBConnection;

import java.sql.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final int MAX_BOOKING_ATTEMPTS = 5;

    public boolean addAppointment(Appointment appointment) throws SQLException {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }

        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required");
        }

        if (appointment.getPatientId() <= 0 || appointment.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Valid patient and doctor IDs are required");
        }

        if (!"SCHEDULED".equals(appointment.getAppointmentType())
                && !"WALK_IN".equals(appointment.getAppointmentType())) {
            throw new IllegalArgumentException("Invalid appointment type");
        }

        String sql = """
                INSERT INTO appointments
                (patient_id, doctor_id, appointment_type,
                 appointment_date, appointment_time, token_number,
                 priority, status, reason)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (int attempt = 1; attempt <= MAX_BOOKING_ATTEMPTS; attempt++) {

            try (Connection con = DBConnection.getConnection()) {

                con.setAutoCommit(false);

                try {
                    int nextToken = getNextTokenNumber(
                            con,
                            appointment.getDoctorId(),
                            appointment.getAppointmentDate()
                    );

                    try (PreparedStatement ps = con.prepareStatement(
                            sql, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, appointment.getPatientId());
                        ps.setInt(2, appointment.getDoctorId());
                        ps.setString(3, appointment.getAppointmentType());

                        ps.setDate(4, Date.valueOf(appointment.getAppointmentDate()));

                        if (appointment.getAppointmentTime() != null) {
                            ps.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
                        } else {
                            ps.setNull(5, Types.TIME);
                        }

                        ps.setInt(6, nextToken);
                        ps.setString(7, appointment.getPriority() == null
                                ? "NORMAL" : appointment.getPriority());
                        ps.setString(8, "WAITING");
                        ps.setString(9, appointment.getReason());

                        int rows = ps.executeUpdate();

                        if (rows != 1) {
                            throw new SQLException("Appointment insertion failed");
                        }

                        int generatedId = 0;

                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                generatedId = keys.getInt(1);
                            }
                        }

                        con.commit();

                        appointment.setAppointmentId(generatedId);
                        appointment.setTokenNumber(nextToken);
                        appointment.setStatus("WAITING");

                        if (appointment.getPriority() == null) {
                            appointment.setPriority("NORMAL");
                        }

                        return true;
                    }

                } catch (SQLException e) {
                    try {
                        con.rollback();
                    } catch (SQLException rollbackError) {
                        e.addSuppressed(rollbackError);
                        throw e;
                    }

                    if (e.getErrorCode() == 1062
                            && attempt < MAX_BOOKING_ATTEMPTS) {
                        continue;
                    }

                    throw e;
                }
            }
        }

        throw new SQLException("Unable to book appointment after retries");
    }

    private int getNextTokenNumber(
            Connection con, int doctorId, LocalDate date
    ) throws SQLException {

        String sql = """
                SELECT COALESCE(MAX(token_number), 0) + 1
                FROM appointments
                WHERE doctor_id = ?
                AND appointment_date = ?
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }

                throw new SQLException("Could not generate token number");
            }
        }
    }

    public List<Appointment> getAllAppointments() throws SQLException {

        List<Appointment> appointments = new ArrayList<>();

        String sql = """
                SELECT * FROM appointments
                ORDER BY appointment_date DESC, token_number ASC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapAppointment(rs));
            }
        }

        return appointments;
    }

    public Appointment getAppointmentById(int appointmentId) throws SQLException {

        String sql = """
                SELECT * FROM appointments
                WHERE appointment_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapAppointment(rs);
                }
            }
        }

        return null;
    }

    public List<Appointment> getWaitingQueue(
            int doctorId, LocalDate date
    ) throws SQLException {

        List<Appointment> queue = new ArrayList<>();

        String sql = """
                SELECT * FROM appointments
                WHERE doctor_id = ?
                AND appointment_date = ?
                AND status = 'WAITING'
                ORDER BY
                    CASE
                        WHEN priority = 'EMERGENCY' THEN 0
                        ELSE 1
                    END,
                    CASE
                        WHEN appointment_type = 'SCHEDULED' THEN 0
                        ELSE 1
                    END,
                    token_number ASC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    queue.add(mapAppointment(rs));
                }
            }
        }

        return queue;
    }

    public boolean updateAppointmentStatus(
            int appointmentId, String newStatus
    ) throws SQLException {

        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid appointment status");
        }

        String sql = """
                UPDATE appointments
                SET status = ?
                WHERE appointment_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, appointmentId);

            return ps.executeUpdate() == 1;
        }
    }

    public Appointment callNextPatient(
            int doctorId, LocalDate date
    ) throws SQLException {

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                String lockSql = """
                        SELECT doctor_id FROM doctors
                        WHERE doctor_id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement lock = con.prepareStatement(lockSql)) {
                    lock.setInt(1, doctorId);

                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Doctor not found");
                        }
                    }
                }

                String activeSql = """
                        SELECT appointment_id FROM appointments
                        WHERE doctor_id = ?
                        AND appointment_date = ?
                        AND status = 'IN_PROGRESS'
                        LIMIT 1
                        """;

                try (PreparedStatement ps = con.prepareStatement(activeSql)) {
                    ps.setInt(1, doctorId);
                    ps.setDate(2, Date.valueOf(date));

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException(
                                    "Doctor already has a patient in progress"
                            );
                        }
                    }
                }

                String selectSql = """
                        SELECT * FROM appointments
                        WHERE doctor_id = ?
                        AND appointment_date = ?
                        AND status = 'WAITING'
                        ORDER BY
                            CASE
                                WHEN priority = 'EMERGENCY' THEN 0
                                ELSE 1
                            END,
                            CASE
                                WHEN appointment_type = 'SCHEDULED' THEN 0
                                ELSE 1
                            END,
                            token_number ASC
                        LIMIT 1
                        """;

                Appointment next = null;

                try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                    ps.setInt(1, doctorId);
                    ps.setDate(2, Date.valueOf(date));

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            next = mapAppointment(rs);
                        }
                    }
                }

                if (next == null) {
                    con.commit();
                    return null;
                }

                String updateSql = """
                        UPDATE appointments
                        SET status = 'IN_PROGRESS'
                        WHERE appointment_id = ?
                        AND status = 'WAITING'
                        """;

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, next.getAppointmentId());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Could not call next patient");
                    }
                }

                con.commit();
                next.setStatus("IN_PROGRESS");

                return next;

            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    public boolean completeAppointment(int appointmentId) throws SQLException {

        String sql = """
                UPDATE appointments
                SET status = 'COMPLETED'
                WHERE appointment_id = ?
                AND status = 'IN_PROGRESS'
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean cancelAppointment(int appointmentId) throws SQLException {

        String sql = """
                UPDATE appointments
                SET status = 'CANCELLED'
                WHERE appointment_id = ?
                AND status = 'WAITING'
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            return ps.executeUpdate() == 1;
        }
    }

    private boolean isValidStatus(String status) {

        return "WAITING".equals(status)
                || "IN_PROGRESS".equals(status)
                || "COMPLETED".equals(status)
                || "CANCELLED".equals(status);
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {

        Appointment appointment = new Appointment();

        appointment.setAppointmentId(rs.getInt("appointment_id"));
        appointment.setPatientId(rs.getInt("patient_id"));
        appointment.setDoctorId(rs.getInt("doctor_id"));
        appointment.setAppointmentType(rs.getString("appointment_type"));

        Date sqlDate = rs.getDate("appointment_date");
        appointment.setAppointmentDate(
                sqlDate == null ? null : sqlDate.toLocalDate()
        );

        Time sqlTime = rs.getTime("appointment_time");
        appointment.setAppointmentTime(
                sqlTime == null ? null : sqlTime.toLocalTime()
        );

        appointment.setTokenNumber(rs.getInt("token_number"));
        appointment.setPriority(rs.getString("priority"));
        appointment.setStatus(rs.getString("status"));
        appointment.setReason(rs.getString("reason"));

        return appointment;
    }
}
