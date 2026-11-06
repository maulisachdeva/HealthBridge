
package com.healthbridge.dao;

import com.healthbridge.model.Patient;
import com.healthbridge.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // 1. Register a new patient
    public boolean addPatient(Patient patient) {

        String sql = "INSERT INTO patients "
                + "(name, age, gender, phone, email, address) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, patient.getName());
            statement.setInt(2, patient.getAge());
            statement.setString(3, patient.getGender());
            statement.setString(4, patient.getPhone());
            statement.setString(5, patient.getEmail());
            statement.setString(6, patient.getAddress());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error registering patient: "
                    + e.getMessage());
            return false;
        }
    }

    // 2. Get all registered patients
    public List<Patient> getAllPatients() {

        List<Patient> patients = new ArrayList<>();

        String sql = "SELECT * FROM patients";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Patient patient = new Patient(
                        resultSet.getInt("patient_id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age"),
                        resultSet.getString("gender"),
                        resultSet.getString("phone"),
                        resultSet.getString("email"),
                        resultSet.getString("address")
                );

                patients.add(patient);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching patients: "
                    + e.getMessage());
        }

        return patients;
    }

    // 3. Search patient by ID
    public Patient getPatientById(int patientId) {

        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return new Patient(
                            resultSet.getInt("patient_id"),
                            resultSet.getString("name"),
                            resultSet.getInt("age"),
                            resultSet.getString("gender"),
                            resultSet.getString("phone"),
                            resultSet.getString("email"),
                            resultSet.getString("address")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching patient: "
                    + e.getMessage());
        }

        return null;
    }

    // 4. Update patient details
    public boolean updatePatient(Patient patient) {

        String sql = "UPDATE patients SET name = ?, age = ?, "
                + "gender = ?, phone = ?, email = ?, address = ? "
                + "WHERE patient_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, patient.getName());
            statement.setInt(2, patient.getAge());
            statement.setString(3, patient.getGender());
            statement.setString(4, patient.getPhone());
            statement.setString(5, patient.getEmail());
            statement.setString(6, patient.getAddress());
            statement.setInt(7, patient.getPatientId());

            int rowsUpdated = statement.executeUpdate();

            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.out.println("Error updating patient: "
                    + e.getMessage());
            return false;
        }
    }

    // 5. Delete patient by ID
    public boolean deletePatient(int patientId) {

        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            int rowsDeleted = statement.executeUpdate();

            return rowsDeleted > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting patient: "
                    + e.getMessage());
            return false;
        }
    }
}
