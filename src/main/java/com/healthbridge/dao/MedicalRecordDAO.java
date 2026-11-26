
package com.healthbridge.dao;

import com.healthbridge.model.MedicalRecord;
import com.healthbridge.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAO {

    // 1. Add a new medical record
    public boolean addMedicalRecord(MedicalRecord record) {

        String sql = "INSERT INTO medical_records " +
                "(patient_id, doctor_id, appointment_id, symptoms, diagnosis, treatment, record_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, record.getPatientId());
            statement.setInt(2, record.getDoctorId());
            statement.setInt(3, record.getAppointmentId());
            statement.setString(4, record.getSymptoms());
            statement.setString(5, record.getDiagnosis());
            statement.setString(6, record.getTreatment());
            statement.setDate(7, record.getRecordDate());

            int rowsInserted = statement.executeUpdate();

            return rowsInserted > 0;

        } catch (SQLException e) {

            System.err.println("========== MEDICAL RECORD ERROR ==========");
            System.err.println("Message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();

            return false;
        }
    }

    // 2. Get all medical records
    public List<MedicalRecord> getAllMedicalRecords() {

        List<MedicalRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM medical_records ORDER BY record_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                records.add(mapMedicalRecord(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching medical records:");
            e.printStackTrace();
        }

        return records;
    }

    // 3. Search medical records by patient ID
    public List<MedicalRecord> getMedicalRecordsByPatientId(int patientId) {

        List<MedicalRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM medical_records WHERE patient_id = ? " +
                "ORDER BY record_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    records.add(mapMedicalRecord(resultSet));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error searching medical records:");
            e.printStackTrace();
        }

        return records;
    }

    // 4. Get a medical record by record ID
    public MedicalRecord getMedicalRecordById(int recordId) {

        String sql = "SELECT * FROM medical_records WHERE record_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, recordId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapMedicalRecord(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching medical record:");
            e.printStackTrace();
        }

        return null;
    }

    // 5. Convert ResultSet row into MedicalRecord object
    private MedicalRecord mapMedicalRecord(ResultSet resultSet)
            throws SQLException {

        return new MedicalRecord(
                resultSet.getInt("record_id"),
                resultSet.getInt("patient_id"),
                resultSet.getInt("doctor_id"),
                resultSet.getInt("appointment_id"),
                resultSet.getString("symptoms"),
                resultSet.getString("diagnosis"),
                resultSet.getString("treatment"),
                resultSet.getDate("record_date")
        );
    }
}
