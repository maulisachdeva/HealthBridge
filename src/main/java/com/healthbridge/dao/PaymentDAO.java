package com.healthbridge.dao;

import com.healthbridge.model.Payment;
import com.healthbridge.util.DBConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // 1. CREATE PAYMENT - TRANSACTION SAFE
    public boolean createPayment(Payment payment) {

        if (payment == null ||
                payment.getBillId() <= 0 ||
                payment.getAmountPaid() <= 0) {

            System.out.println("Invalid payment details.");
            return false;
        }

        String billSQL =
                "SELECT total_amount FROM bills WHERE bill_id = ? FOR UPDATE";

        String paidSQL =
                "SELECT COALESCE(SUM(amount_paid), 0) " +
                "FROM payments WHERE bill_id = ? AND payment_status = 'SUCCESS'";

        String duplicateSQL =
                "SELECT COUNT(*) FROM payments " +
                "WHERE transaction_reference = ? " +
                "AND payment_status = 'SUCCESS'";

        String insertSQL =
                "INSERT INTO payments " +
                "(bill_id, amount_paid, payment_method, " +
                "transaction_reference, payment_status) " +
                "VALUES (?, ?, ?, ?, ?)";

        String updateSQL =
                "UPDATE bills SET payment_status = ? WHERE bill_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                // STEP 1: Lock bill row
                BigDecimal totalAmount;

                try (PreparedStatement pst =
                             con.prepareStatement(billSQL)) {

                    pst.setInt(1, payment.getBillId());

                    try (ResultSet rs = pst.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException("Bill not found!");
                        }

                        totalAmount = rs.getBigDecimal("total_amount");
                    }
                }

                // STEP 2: Calculate already paid amount
                BigDecimal paidAmount;

                try (PreparedStatement pst =
                             con.prepareStatement(paidSQL)) {

                    pst.setInt(1, payment.getBillId());

                    try (ResultSet rs = pst.executeQuery()) {

                        rs.next();

                        paidAmount = rs.getBigDecimal(1);
                    }
                }

                BigDecimal paymentAmount =
                        BigDecimal.valueOf(payment.getAmountPaid());

                BigDecimal remaining =
                        totalAmount.subtract(paidAmount);

                // STEP 3: Validate payment
                if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new SQLException(
                            "Payment must be greater than zero."
                    );
                }

                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new SQLException(
                            "This bill is already fully paid."
                    );
                }

                if (paymentAmount.compareTo(remaining) > 0) {
                    throw new SQLException(
                            "Overpayment not allowed! Remaining amount: "
                                    + remaining
                    );
                }

                // STEP 4: Prevent duplicate transaction references
                String reference =
                        payment.getTransactionReference();

                if (reference != null && !reference.trim().isEmpty()) {

                    try (PreparedStatement pst =
                                 con.prepareStatement(duplicateSQL)) {

                        pst.setString(1, reference.trim());

                        try (ResultSet rs = pst.executeQuery()) {

                            if (rs.next() && rs.getInt(1) > 0) {

                                throw new SQLException(
                                        "Duplicate transaction reference!"
                                );
                            }
                        }
                    }
                }

                // STEP 5: Insert payment
                try (PreparedStatement pst =
                             con.prepareStatement(
                                     insertSQL,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    pst.setInt(1, payment.getBillId());
                    pst.setBigDecimal(2, paymentAmount);
                    pst.setString(3, payment.getPaymentMethod());
                    pst.setString(4, reference);
                    pst.setString(5, "SUCCESS");

                    pst.executeUpdate();

                    try (ResultSet rs = pst.getGeneratedKeys()) {

                        if (rs.next()) {
                            payment.setPaymentId(rs.getInt(1));
                        }
                    }
                }

                // STEP 6: Calculate updated balance
                BigDecimal newPaidAmount =
                        paidAmount.add(paymentAmount);

                String status;

                if (newPaidAmount.compareTo(totalAmount) >= 0) {
                    status = "PAID";
                } else if (
                        newPaidAmount.compareTo(BigDecimal.ZERO) > 0
                ) {
                    status = "PARTIAL";
                } else {
                    status = "PENDING";
                }

                // STEP 7: Update bill status
                try (PreparedStatement pst =
                             con.prepareStatement(updateSQL)) {

                    pst.setString(1, status);
                    pst.setInt(2, payment.getBillId());

                    pst.executeUpdate();
                }

                // STEP 8: Commit
                con.commit();

                payment.setPaymentStatus("SUCCESS");

                System.out.println(
                        "Payment recorded successfully!"
                );

                System.out.println("Bill Status: " + status);

                return true;

            } catch (SQLException e) {

                con.rollback();

                System.out.println(
                        "Payment Transaction Failed: " + e.getMessage()
                );

                return false;

            } finally {

                con.setAutoCommit(true);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Payment Database Error: " + e.getMessage()
            );

            return false;
        }
    }


    // 2. GET PAYMENT BY ID
    public Payment getPaymentById(int paymentId) {

        String sql =
                "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, paymentId);

            try (ResultSet rs = pst.executeQuery()) {

                if (rs.next()) {
                    return extractPayment(rs);
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error fetching payment: " + e.getMessage()
            );
        }

        return null;
    }


    // 3. GET ALL PAYMENTS
    public List<Payment> getAllPayments() {

        List<Payment> payments = new ArrayList<>();

        String sql =
                "SELECT * FROM payments ORDER BY payment_date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                payments.add(extractPayment(rs));
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error fetching payments: " + e.getMessage()
            );
        }

        return payments;
    }


    // 4. GET PAYMENTS BY BILL ID
    public List<Payment> getPaymentsByBillId(int billId) {

        List<Payment> payments = new ArrayList<>();

        String sql =
                "SELECT * FROM payments WHERE bill_id = ? " +
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

            System.out.println(
                    "Error searching payments: " + e.getMessage()
            );
        }

        return payments;
    }


    // 5. GET TOTAL PAID AMOUNT
    public double getTotalPaidAmount() {

        String sql =
                "SELECT COALESCE(SUM(amount_paid), 0) " +
                "FROM payments WHERE payment_status = 'SUCCESS'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error calculating total payments: "
                            + e.getMessage()
            );
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

            System.out.println(
                    "Error counting payments: " + e.getMessage()
            );
        }

        return 0;
    }


    // 7. EXTRACT PAYMENT
    private Payment extractPayment(ResultSet rs)
            throws SQLException {

        Payment payment = new Payment();

        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBillId(rs.getInt("bill_id"));
        payment.setAmountPaid(rs.getDouble("amount_paid"));

        payment.setPaymentMethod(
                rs.getString("payment_method")
        );

        Timestamp timestamp = rs.getTimestamp("payment_date");

        if (timestamp != null) {
            payment.setPaymentDate(timestamp.toLocalDateTime());
        }

        payment.setTransactionReference(
                rs.getString("transaction_reference")
        );

        payment.setPaymentStatus(
                rs.getString("payment_status")
        );

        return payment;
    }
}