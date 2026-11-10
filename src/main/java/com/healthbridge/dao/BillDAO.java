package com.healthbridge.dao;

import com.healthbridge.model.Bill;
import com.healthbridge.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    // 1. CREATE NEW BILL
    public boolean createBill(Bill bill) {

        String sql = "INSERT INTO bills " +
                "(patient_id, appointment_id, consultation_charge, " +
                "medicine_charge, other_charge, total_amount, " +
                "payment_status, bill_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, bill.getPatientId());
            pst.setInt(2, bill.getAppointmentId());
            pst.setDouble(3, bill.getConsultationCharge());
            pst.setDouble(4, bill.getMedicineCharge());
            pst.setDouble(5, bill.getOtherCharge());
            pst.setDouble(6, bill.getTotalAmount());
            pst.setString(7, bill.getPaymentStatus());
            pst.setDate(8, Date.valueOf(bill.getBillDate()));

            int rows = pst.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        bill.setBillId(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error creating bill: " + e.getMessage());
        }

        return false;
    }


    // 2. GET BILL USING BILL ID
    public Bill getBillById(int billId) {

        String sql = "SELECT * FROM bills WHERE bill_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, billId);

            try (ResultSet rs = pst.executeQuery()) {

                if (rs.next()) {
                    return extractBill(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching bill: " + e.getMessage());
        }

        return null;
    }


    // 3. GET ALL BILLS
    public List<Bill> getAllBills() {

        List<Bill> bills = new ArrayList<>();

        String sql = "SELECT * FROM bills ORDER BY bill_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                bills.add(extractBill(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching bills: " + e.getMessage());
        }

        return bills;
    }


    // 4. SEARCH BILLS USING PATIENT ID
    public List<Bill> getBillsByPatientId(int patientId) {

        List<Bill> bills = new ArrayList<>();

        String sql = "SELECT * FROM bills WHERE patient_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, patientId);

            try (ResultSet rs = pst.executeQuery()) {

                while (rs.next()) {
                    bills.add(extractBill(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching bills: " + e.getMessage());
        }

        return bills;
    }


    // 5. UPDATE PAYMENT STATUS
    public boolean updatePaymentStatus(int billId, String status) {

        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, status);
            pst.setInt(2, billId);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating bill status: " + e.getMessage());
        }

        return false;
    }


    // 6. DELETE BILL
    public boolean deleteBill(int billId) {

        String sql = "DELETE FROM bills WHERE bill_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, billId);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting bill: " + e.getMessage());
        }

        return false;
    }


    // 7. CONVERT RESULTSET INTO BILL OBJECT
    private Bill extractBill(ResultSet rs) throws SQLException {

        Bill bill = new Bill();

        bill.setBillId(rs.getInt("bill_id"));
        bill.setPatientId(rs.getInt("patient_id"));
        bill.setAppointmentId(rs.getInt("appointment_id"));

        bill.setConsultationCharge(
                rs.getDouble("consultation_charge"));

        bill.setMedicineCharge(
                rs.getDouble("medicine_charge"));

        bill.setOtherCharge(
                rs.getDouble("other_charge"));

        bill.setTotalAmount(
                rs.getDouble("total_amount"));

        bill.setPaymentStatus(
                rs.getString("payment_status"));

        bill.setBillDate(
                rs.getDate("bill_date").toLocalDate());

        return bill;
    }
}

