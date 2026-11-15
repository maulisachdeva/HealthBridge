package com.healthbridge.dao;

import com.healthbridge.model.Payment;
import com.healthbridge.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // 1. Create a new payment
    public boolean createPayment(Payment payment) {

        if (payment.getAmountPaid() <= 0) {
            System.out.println("Payment amount must be greater than zero.");
            return false;
        }

        String insertSql = "INSERT INTO payments " +
                "(bill_id, amount_paid, payment_method, " +
                "transaction_reference, payment_status) " +
                "VALUES (?, ?, ?, ?, 'SUCCESS')";

        String billSql = "SELECT total_amount FROM bills WHERE bill_id = ?";

        String paidSql = "SELECT COALESCE(SUM(amount_paid), 0) " +
                "FROM payments WHERE bill_id = ? AND payment_status = 'SUCCESS'";

        String updateSql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();

            if (connection == null) {
                return false;
            }

            connection.setAutoCommit(false);

            double totalAmount;

            try (PreparedStatement billStatement =
                         connection.prepareStatement(billSql)) {

                billStatement.setInt(1, payment.getBillId());

                try (ResultSet result = billStatement.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException("Bill not found.");
                    }

                    totalAmount = result.getDouble("total_amount");
                }
            }

            double alreadyPaid;

            try (PreparedStatement paidStatement =
                         connection.prepareStatement(paidSql)) {

                paidStatement.setInt(1, payment.getBillId());

                try (ResultSet result = paidStatement.executeQuery()) {
                    result.next();
                    alreadyPaid = result.getDouble(1);
                }
            }

            if (alreadyPaid + payment.getAmountPaid() > totalAmount) {
                throw new SQLException("Payment exceeds remaining bill amount.");
            }

            try (PreparedStatement insertStatement =
                         connection.prepareStatement(
                                 insertSql, Statement.RETURN_GENERATED_KEYS)) {

                insertStatement.setInt(1, payment.getBillId());
                insertStatement.setDouble(2, payment.getAmountPaid());
                insertStatement.setString(3, payment.getPaymentMethod());
                insertStatement.setString(4, payment.getTransactionReference());

                insertStatement.executeUpdate();

                try (ResultSet keys = insertStatement.getGeneratedKeys()) {
                    if (keys.next()) {
                        payment.setPaymentId(keys.getInt(1));
                    }
                }
            }

            double totalPaid = alreadyPaid + payment.getAmountPaid();

            String status = totalPaid >= totalAmount ? "PAID" : "PENDING";

            try (PreparedStatement updateStatement =
                         connection.prepareStatement(updateSql)) {

                updateStatement.setString(1, status);
                updateStatement.setInt(2, payment.getBillId());

                updateStatement.executeUpdate();
            }

            connection.commit();

            payment.setPaymentStatus("SUCCESS");
            payment.setPaymentDate(java.time.LocalDateTime.now());

            return true;

        } catch (SQLException e) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            System.out.println("Error creating payment: " + e.getMessage());
            return false;

        } finally {

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 2. Get payment by ID
    public Payment getPaymentById(int paymentId) {

        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, paymentId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return extractPayment(result);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching payment: " + e.getMessage());
        }

        return null;
    }

    // 3. Get all payments
    public List<Payment> getAllPayments() {

        List<Payment> payments = new ArrayList<>();

        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                payments.add(extractPayment(result));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching payments: " + e.getMessage());
        }

        return payments;
    }

    // 4. Get payments by bill ID
    public List<Payment> getPaymentsByBillId(int billId) {

        List<Payment> payments = new ArrayList<>();

        String sql = "SELECT * FROM payments WHERE bill_id = ? " +
                "ORDER BY payment_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, billId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    payments.add(extractPayment(result));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching bill payments: " + e.getMessage());
        }

        return payments;
    }

    // 5. Get total successful payment amount
    public double getTotalPaidAmount() {

        String sql = "SELECT COALESCE(SUM(amount_paid), 0) " +
                "FROM payments WHERE payment_status = 'SUCCESS'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getDouble(1);
            }

        } catch (SQLException e) {
            System.out.println("Error calculating payments: " + e.getMessage());
        }

        return 0;
    }

    // 6. Get total payment count
    public int getTotalPaymentCount() {

        String sql = "SELECT COUNT(*) FROM payments " +
                "WHERE payment_status = 'SUCCESS'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Error counting payments: " + e.getMessage());
        }

        return 0;
    }

    // Helper method
    private Payment extractPayment(ResultSet result) throws SQLException {

        Payment payment = new Payment();

        payment.setPaymentId(result.getInt("payment_id"));
        payment.setBillId(result.getInt("bill_id"));
        payment.setAmountPaid(result.getDouble("amount_paid"));
        payment.setPaymentMethod(result.getString("payment_method"));
        payment.setTransactionReference(
                result.getString("transaction_reference"));
        payment.setPaymentStatus(result.getString("payment_status"));

        Timestamp timestamp = result.getTimestamp("payment_date");

        if (timestamp != null) {
            payment.setPaymentDate(timestamp.toLocalDateTime());
        }

        return payment;
    }
}