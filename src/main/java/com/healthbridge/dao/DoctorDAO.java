
package com.healthbridge.dao;

import com.healthbridge.model.Doctor;
import com.healthbridge.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    // 1. Register a new doctor
    public boolean addDoctor(Doctor doctor) {

        String sql = "INSERT INTO doctors "
                + "(name, specialization, phone, email) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, doctor.getName());
            statement.setString(2, doctor.getSpecialization());
            statement.setString(3, doctor.getPhone());
            statement.setString(4, doctor.getEmail());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error registering doctor: "
                    + e.getMessage());
            return false;
        }
    }

    // 2. Get all registered doctors
    public List<Doctor> getAllDoctors() {

        List<Doctor> doctors = new ArrayList<>();

        String sql = "SELECT * FROM doctors";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Doctor doctor = new Doctor(
                        resultSet.getInt("doctor_id"),
                        resultSet.getString("name"),
                        resultSet.getString("specialization"),
                        resultSet.getString("phone"),
                        resultSet.getString("email")
                );

                doctors.add(doctor);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching doctors: "
                    + e.getMessage());
        }

        return doctors;
    }

    // 3. Search doctor by ID
    public Doctor getDoctorById(int doctorId) {

        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, doctorId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return new Doctor(
                            resultSet.getInt("doctor_id"),
                            resultSet.getString("name"),
                            resultSet.getString("specialization"),
                            resultSet.getString("phone"),
                            resultSet.getString("email")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching doctor: "
                    + e.getMessage());
        }

        return null;
    }

    // 4. Update doctor details
    public boolean updateDoctor(Doctor doctor) {

        String sql = "UPDATE doctors SET name = ?, "
                + "specialization = ?, phone = ?, email = ? "
                + "WHERE doctor_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, doctor.getName());
            statement.setString(2, doctor.getSpecialization());
            statement.setString(3, doctor.getPhone());
            statement.setString(4, doctor.getEmail());
            statement.setInt(5, doctor.getDoctorId());

            int rowsUpdated = statement.executeUpdate();

            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.out.println("Error updating doctor: "
                    + e.getMessage());
            return false;
        }
    }

    // 5. Delete doctor by ID
    public boolean deleteDoctor(int doctorId) {

        String sql = "DELETE FROM doctors WHERE doctor_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, doctorId);

            int rowsDeleted = statement.executeUpdate();

            return rowsDeleted > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting doctor: "
                    + e.getMessage());
            return false;
        }
    }
}
