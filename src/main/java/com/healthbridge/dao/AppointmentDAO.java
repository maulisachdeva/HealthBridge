package com.healthbridge.dao;

import com.healthbridge.model.Appointment;
import com.healthbridge.util.DBConnection;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;

public class AppointmentDAO {

    public void addAppointment(Appointment appointment) throws SQLException {

        String sql = """
                INSERT INTO appointments
                (patient_id, doctor_id, appointment_type,
                appointment_date, appointment_time, token_number,
                priority, status, reason)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){

        }

    }

}
