package com.healthbridge.dao;

import com.healthbridge.model.Prescription;
import com.healthbridge.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    // 1. Add prescription
    public boolean addPrescription(Prescription prescription) {

        String sql = "INSERT INTO prescriptions " +
                "(record_id, medicine_name, dosage, frequency, duration, instructions) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, prescription.getRecordId());
            ps.setString(2, prescription.getMedicineName());
            ps.setString(3, prescription.getDosage());
            ps.setString(4, prescription.getFrequency());
            ps.setString(5, prescription.getDuration());
            ps.setString(6, prescription.getInstructions());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding prescription: " + e.getMessage());
            return false;
        }
    }

    // 2. Get prescriptions using medical record ID
    public List<Prescription> getPrescriptionsByRecordId(int recordId) {

        List<Prescription> prescriptions = new ArrayList<>();

        String sql = "SELECT * FROM prescriptions WHERE record_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, recordId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Prescription prescription = new Prescription(
                            rs.getInt("prescription_id"),
                            rs.getInt("record_id"),
                            rs.getString("medicine_name"),
                            rs.getString("dosage"),
                            rs.getString("frequency"),
                            rs.getString("duration"),
                            rs.getString("instructions")
                    );

                    prescriptions.add(prescription);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching prescriptions: " + e.getMessage());
        }

        return prescriptions;
    }

    // 3. Get all prescriptions
    public List<Prescription> getAllPrescriptions() {

        List<Prescription> prescriptions = new ArrayList<>();

        String sql = "SELECT * FROM prescriptions";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Prescription prescription = new Prescription(
                        rs.getInt("prescription_id"),
                        rs.getInt("record_id"),
                        rs.getString("medicine_name"),
                        rs.getString("dosage"),
                        rs.getString("frequency"),
                        rs.getString("duration"),
                        rs.getString("instructions")
                );

                prescriptions.add(prescription);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching prescriptions: " + e.getMessage());
        }

        return prescriptions;
    }
}
