package com.healthbridge.dao;

import com.healthbridge.model.Bill;
import com.healthbridge.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    // 1. Create a new bill
    public boolean createBill(Bill bill) {

        String sql = "INSERT INTO bills " +
                "(patient_id, appointment_id, consultation_charge, " +
                "medicine_charge, other_charge, total_amount, " +
                "payment_status, bill_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, bill.getPatientId());
            statement.setInt(2, bill.getAppointmentId());
            statement.setDouble(3, bill.getConsultationCharge());
            statement.setDouble(4, bill.getMedicineCharge());
            statement.setDouble(5, bill.getOtherCharge());
            statement.setDouble(6, bill.getTotalAmount());
            statement.setString(7, bill.getPaymentStatus());
            statement.setDate(8, Date.valueOf(bill.getBillDate()));

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        bill.setBillId(keys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error creating bill: " + e.getMessage());
        }

        return false;
    }

    // 2. Get bill by ID
    public Bill getBillById(int billId) {

        String sql = "SELECT * FROM bills WHERE bill_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, billId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return extractBill(result);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching bill: " + e.getMessage());
        }

        return null;
    }

    // 3. Get all bills
    public List<Bill> getAllBills() {

        List<Bill> bills = new ArrayList<>();

        String sql = "SELECT * FROM bills ORDER BY bill_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                bills.add(extractBill(result));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching bills: " + e.getMessage());
        }

        return bills;
    }

    // 4. Get bills by patient ID
    public List<Bill> getBillsByPatientId(int patientId) {

        List<Bill> bills = new ArrayList<>();

        String sql = "SELECT * FROM bills WHERE patient_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    bills.add(extractBill(result));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching patient bills: " + e.getMessage());
        }

        return bills;
    }

    // 5. Update payment status
    public boolean updatePaymentStatus(int billId, String status) {

        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, billId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating bill status: " + e.getMessage());
        }

        return false;
    }

    // 6. Get total revenue from fully paid bills
    public double getTotalRevenue() {

        String sql = "SELECT COALESCE(SUM(total_amount), 0) " +
                "FROM bills WHERE payment_status = 'PAID'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getDouble(1);
            }

        } catch (SQLException e) {
            System.out.println("Error calculating revenue: " + e.getMessage());
        }

        return 0;
    }

    // 7. Get pending amount
    public double getPendingAmount() {

        String sql = "SELECT COALESCE(SUM(total_amount), 0) " +
                "FROM bills WHERE payment_status = 'PENDING'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getDouble(1);
            }

        } catch (SQLException e) {
            System.out.println("Error calculating pending amount: " + e.getMessage());
        }

        return 0;
    }

    // 8. Get total bill count
    public int getTotalBillCount() {

        String sql = "SELECT COUNT(*) FROM bills";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Error counting bills: " + e.getMessage());
        }

        return 0;
    }

    // Helper method to convert ResultSet into Bill object
    private Bill extractBill(ResultSet result) throws SQLException {

        Bill bill = new Bill();

        bill.setBillId(result.getInt("bill_id"));
        bill.setPatientId(result.getInt("patient_id"));
        bill.setAppointmentId(result.getInt("appointment_id"));

        bill.setConsultationCharge(result.getDouble("consultation_charge"));
        bill.setMedicineCharge(result.getDouble("medicine_charge"));
        bill.setOtherCharge(result.getDouble("other_charge"));
        bill.setTotalAmount(result.getDouble("total_amount"));

        bill.setPaymentStatus(result.getString("payment_status"));

        Date billDate = result.getDate("bill_date");

        if (billDate != null) {
            bill.setBillDate(billDate.toLocalDate());
        }

        return bill;
    }
}