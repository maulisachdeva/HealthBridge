package com.healthbridge.dao;

import com.healthbridge.model.Payment;
import com.healthbridge.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // 1. CREATE NEW PAYMENT
    public boolean createPayment(Payment payment) {

        String insertSQL = "INSERT INTO payments " +
                "(bill_id, amount_paid, payment_method, " +
                "transaction_reference, payment_status) " +
                "VALUES (?, ?, ?, ?, ?)";

        String totalSQL = "SELECT total_amount FROM bills WHERE bill_id = ?";

        String paidSQL = "SELECT COALESCE(SUM(amount_paid), 0) " +
                "FROM payments WHERE bill_id = ? AND payment_status = 'SUCCESS'";

        String updateSQL = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                // Insert payment
                try (PreparedStatement pst = con.prepareStatement(
                        insertSQL, Statement.RETURN_GENERATED_KEYS)) {

                    pst.setInt(1, payment.getBillId());
                    pst.setDouble(2, payment.getAmountPaid());
                    pst.setString(3, payment.getPaymentMethod());
                    pst.setString(4, payment.getTransactionReference());
                    pst.setString(5, "SUCCESS");

                    pst.executeUpdate();

                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) {
                            payment.setPaymentId(rs.getInt(1));
                        }
                    }
                }

                // Get bill amount
                double totalAmount;

                try (PreparedStatement pst = con.prepareStatement(totalSQL)) {

                    pst.setInt(1, payment.getBillId());

                    try (ResultSet rs = pst.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException("Bill not found!");
                        }

                        totalAmount = rs.getDouble(1);
                    }
                }

                // Calculate total paid amount
                double paidAmount;

                try (PreparedStatement pst = con.prepareStatement(paidSQL)) {

                    pst.setInt(1, payment.getBillId());

                    try (ResultSet rs = pst.executeQuery()) {
                        rs.next();
                        paidAmount = rs.getDouble(1);
                    }
                }

                // Update bill status
                String status = paidAmount >= totalAmount
                        ? "PAID" : "PENDING";

                try (PreparedStatement pst = con.prepareStatement(updateSQL)) {

                    pst.setString(1, status);
                    pst.setInt(2, payment.getBillId());

                    pst.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {

                con.rollback();
                throw e;

            } finally {

                con.setAutoCommit(true);
            }

        } catch (SQLException e) {

            System.out.println("Payment Error: " + e.getMessage());
        }

        return false;
    }


    // 2. GET PAYMENT USING PAYMENT ID
    public Payment getPaymentById(int paymentId) {

        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, paymentId);

            try (ResultSet rs = pst.executeQuery()) {

                if (rs.next()) {
                    return extractPayment(rs);
                }
            }

        } catch (SQLException e) {

            System.out.println("Error fetching payment: " + e.getMessage());
        }

        return null;
    }


    // 3. GET ALL PAYMENTS
    public List<Payment> getAllPayments() {

        List<Payment> payments = new ArrayList<>();

        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                payments.add(extractPayment(rs));
            }

        } catch (SQLException e) {

            System.out.println("Error fetching payments: " + e.getMessage());
        }

        return payments;
    }


    // 4. GET PAYMENTS BY BILL ID
    public List<Payment> getPaymentsByBillId(int billId) {

        List<Payment> payments = new ArrayList<>();

        String sql = "SELECT * FROM payments WHERE bill_id = ? " +
                "ORDER BY payment_date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, billId);

            try (ResultSet rs = pst.executeQuery()) {

                while (rs.next()) {
                    payments.add(extractPayment(rs));
                }
            }

        } catch (SQLException e) {

            System.out.println("Error searching payments: " + e.getMessage());
        }

        return payments;
    }


    // 5. GET TOTAL PAID AMOUNT
    public double getTotalPaidAmount() {

        String sql = "SELECT COALESCE(SUM(amount_paid), 0) " +
                "FROM payments WHERE payment_status = 'SUCCESS'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {

            System.out.println("Error calculating payments: " + e.getMessage());
        }

        return 0.0;
    }


    // 6. GET TOTAL PAYMENT COUNT
    public int getTotalPaymentCount() {

        String sql = "SELECT COUNT(*) FROM payments";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {

            System.out.println("Error counting payments: " + e.getMessage());
        }

        return 0;
    }


    // 7. CONVERT RESULTSET INTO PAYMENT OBJECT
    private Payment extractPayment(ResultSet rs) throws SQLException {

        Payment payment = new Payment();

        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBillId(rs.getInt("bill_id"));
        payment.setAmountPaid(rs.getDouble("amount_paid"));
        payment.setPaymentMethod(rs.getString("payment_method"));

        payment.setPaymentDate(
                rs.getTimestamp("payment_date").toLocalDateTime()
        );

        payment.setTransactionReference(
                rs.getString("transaction_reference")
        );

        payment.setPaymentStatus(
                rs.getString("payment_status")
        );

        return payment;
    }
}