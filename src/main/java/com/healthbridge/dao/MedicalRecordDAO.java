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

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding medical record: " + e.getMessage());
            return false;
        }
    }

    // 2. Get all medical records
    public List<MedicalRecord> getAllMedicalRecords() {
        List<MedicalRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM medical_records";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                MedicalRecord record = new MedicalRecord(
                        resultSet.getInt("record_id"),
                        resultSet.getInt("patient_id"),
                        resultSet.getInt("doctor_id"),
                        resultSet.getInt("appointment_id"),
                        resultSet.getString("symptoms"),
                        resultSet.getString("diagnosis"),
                        resultSet.getString("treatment"),
                        resultSet.getDate("record_date")
                );

                records.add(record);


            }

        } catch (SQLException e) {
            System.out.println("Error fetching medical records: " + e.getMessage());
        }



        return records;
    }

    // 3. Search medical records using patient ID
    public List<MedicalRecord> getMedicalRecordsByPatientId(int patientId) {
        List<MedicalRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM medical_records WHERE patient_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {

                    MedicalRecord record = new MedicalRecord(
                            resultSet.getInt("record_id"),
                            resultSet.getInt("patient_id"),
                            resultSet.getInt("doctor_id"),
                            resultSet.getInt("appointment_id"),
                            resultSet.getString("symptoms"),
                            resultSet.getString("diagnosis"),
                            resultSet.getString("treatment"),
                            resultSet.getDate("record_date")
                    );

                    records.add(record);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching medical records: " + e.getMessage());
        }

        return records;
    }

    // 4. Get medical record using record ID
    public MedicalRecord getMedicalRecordById(int recordId) {
        String sql = "SELECT * FROM medical_records WHERE record_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, recordId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {

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

        } catch (SQLException e) {
            System.out.println("Error fetching medical record: " + e.getMessage());
        }

        return null;
    }
}