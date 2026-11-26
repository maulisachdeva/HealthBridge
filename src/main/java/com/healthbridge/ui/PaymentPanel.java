package com.healthbridge.ui;

import com.healthbridge.dao.PaymentDAO;
import com.healthbridge.model.Payment;
import com.healthbridge.util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.List;

public class PaymentPanel extends JPanel {

    private final Color DARK_BLUE = new Color(25, 45, 85);
    private final Color BACKGROUND = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final DecimalFormat df = new DecimalFormat("0.00");

    private JTextField billIdField;
    private JTextField amountField;
    private JTextField referenceField;

    private JComboBox<String> paymentMethodBox;

    private JLabel patientLabel;
    private JLabel appointmentLabel;
    private JLabel totalLabel;
    private JLabel paidLabel;
    private JLabel balanceLabel;
    private JLabel statusLabel;

    private JTable paymentTable;
    private DefaultTableModel tableModel;

    private double totalAmount;
    private double paidAmount;
    private int currentBillId = -1;

    public PaymentPanel() {
    this(0);
}

public PaymentPanel(int billId) {

    setLayout(new BorderLayout(15, 15));
    setBackground(BACKGROUND);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    add(createHeader(), BorderLayout.NORTH);
    add(createMainPanel(), BorderLayout.CENTER);

    if (billId > 0) {
        billIdField.setText(String.valueOf(billId));
        SwingUtilities.invokeLater(this::loadBill);
    }
}

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        JLabel title = new JLabel("Payment Management");

        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Record and manage patient bill payments"
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(BACKGROUND);

        textPanel.add(title);
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createMainPanel() {

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BACKGROUND);

        JPanel top = new JPanel(new GridLayout(1, 2, 15, 0));
        top.setBackground(BACKGROUND);

        top.add(createBillPanel());
        top.add(createPaymentForm());

        main.add(top, BorderLayout.NORTH);
        main.add(createHistoryPanel(), BorderLayout.CENTER);

        return main;
    }

    private JPanel createBillPanel() {

        JPanel panel = createCard("Bill Details");

        panel.setLayout(new GridLayout(0, 2, 10, 12));

        billIdField = new JTextField();

        JButton searchButton = createButton("Load Bill");

        patientLabel = new JLabel("-");
        appointmentLabel = new JLabel("-");
        totalLabel = new JLabel("₹ 0.00");
        paidLabel = new JLabel("₹ 0.00");
        balanceLabel = new JLabel("₹ 0.00");
        statusLabel = new JLabel("-");

        panel.add(new JLabel("Bill ID"));
        panel.add(createSearchPanel(billIdField, searchButton));

        panel.add(new JLabel("Patient ID"));
        panel.add(patientLabel);

        panel.add(new JLabel("Appointment ID"));
        panel.add(appointmentLabel);

        panel.add(new JLabel("Total Amount"));
        panel.add(totalLabel);

        panel.add(new JLabel("Already Paid"));
        panel.add(paidLabel);

        panel.add(new JLabel("Remaining Balance"));
        panel.add(balanceLabel);

        panel.add(new JLabel("Payment Status"));
        panel.add(statusLabel);

        searchButton.addActionListener(e -> loadBill());

        return panel;
    }

    private JPanel createPaymentForm() {

        JPanel panel = createCard("Record Payment");

        panel.setLayout(new GridLayout(0, 2, 10, 12));

        amountField = new JTextField();
        referenceField = new JTextField();

        paymentMethodBox = new JComboBox<>(
                new String[]{"Cash", "UPI", "Card", "Net Banking"}
        );

        JButton payButton = createButton("Record Payment");
        JButton clearButton = createButton("Clear");

        panel.add(new JLabel("Payment Amount"));
        panel.add(amountField);

        panel.add(new JLabel("Payment Method"));
        panel.add(paymentMethodBox);

        panel.add(new JLabel("Transaction Reference"));
        panel.add(referenceField);

        panel.add(payButton);
        panel.add(clearButton);

        payButton.addActionListener(e -> recordPayment());

        clearButton.addActionListener(e -> clearForm());

        return panel;
    }

    private JPanel createHistoryPanel() {

        JPanel panel = createCard("Payment History");

        panel.setLayout(new BorderLayout(5, 10));

        String[] columns = {
                "Payment ID",
                "Bill ID",
                "Amount",
                "Method",
                "Reference",
                "Status",
                "Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentTable = new JTable(tableModel);

        paymentTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(paymentTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchPanel(
            JTextField field,
            JButton button
    ) {

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(WHITE);

        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCard(String title) {

        JPanel panel = new JPanel();

        panel.setBackground(WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 225, 235)
                        ),
                        BorderFactory.createTitledBorder(title)
                )
        );

        return panel;
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setBackground(DARK_BLUE);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));

        return button;
    }

    // LOAD BILL DETAILS

    private void loadBill() {

        try {

            int billId = Integer.parseInt(
                    billIdField.getText().trim()
            );

            if (billId <= 0) {
                throw new NumberFormatException();
            }

            String sql = """
                    SELECT bill_id, patient_id, appointment_id,
                           total_amount, payment_status
                    FROM bills
                    WHERE bill_id = ?
                    """;

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setInt(1, billId);

                try (ResultSet rs = pst.executeQuery()) {

                    if (!rs.next()) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Bill not found!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );

                        resetBillDetails();
                        return;
                    }

                    currentBillId = rs.getInt("bill_id");

                    patientLabel.setText(
                            String.valueOf(rs.getInt("patient_id"))
                    );

                    appointmentLabel.setText(
                            String.valueOf(rs.getInt("appointment_id"))
                    );

                    totalAmount = rs.getDouble("total_amount");

                    totalLabel.setText("₹ " + df.format(totalAmount));

                    statusLabel.setText(
                            rs.getString("payment_status")
                    );
                }
            }

            refreshPaymentDetails();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid Bill ID."
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load bill: " + e.getMessage()
            );
        }
    }

    // REFRESH PAYMENT HISTORY AND BALANCE

    private void refreshPaymentDetails() {

        if (currentBillId <= 0) {
            return;
        }

        List<Payment> payments =
                paymentDAO.getPaymentsByBillId(currentBillId);

        tableModel.setRowCount(0);

        paidAmount = 0;

        for (Payment payment : payments) {

            if ("SUCCESS".equalsIgnoreCase(
                    payment.getPaymentStatus()
            )) {

                paidAmount += payment.getAmountPaid();
            }

            tableModel.addRow(new Object[]{
                    payment.getPaymentId(),
                    payment.getBillId(),
                    "₹ " + df.format(payment.getAmountPaid()),
                    payment.getPaymentMethod(),
                    payment.getTransactionReference(),
                    payment.getPaymentStatus(),
                    payment.getPaymentDate()
            });
        }

        double balance = Math.max(0, totalAmount - paidAmount);

        paidLabel.setText("₹ " + df.format(paidAmount));
        balanceLabel.setText("₹ " + df.format(balance));

        if (balance <= 0) {
            statusLabel.setText("PAID");
        } else if (paidAmount > 0) {
            statusLabel.setText("PARTIAL");
        } else {
            statusLabel.setText("PENDING");
        }
    }

    // RECORD PAYMENT

    private void recordPayment() {

        if (currentBillId <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please load a bill first."
            );

            return;
        }

        double amount;

        try {

            amount = Double.parseDouble(
                    amountField.getText().trim()
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid payment amount."
            );

            return;
        }

        if (amount <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Payment amount must be greater than zero."
            );

            return;
        }

        double remaining = totalAmount - paidAmount;

        if (remaining <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "This bill is already fully paid."
            );

            return;
        }

        if (amount > remaining + 0.001) {

            JOptionPane.showMessageDialog(
                    this,
                    "Overpayment not allowed!\nRemaining: ₹ "
                            + df.format(remaining)
            );

            return;
        }

        String method = paymentMethodBox.getSelectedItem().toString();

        String reference = referenceField.getText().trim();

        if (!"Cash".equals(method) && reference.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Transaction reference is required for "
                            + method + " payments."
            );

            return;
        }

        Payment payment = new Payment(
                currentBillId,
                amount,
                method,
                reference
        );

        boolean success = paymentDAO.createPayment(payment);

        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Payment recorded successfully!"
            );

            amountField.setText("");
            referenceField.setText("");

            loadBill();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Payment failed. Please check the bill and database."
            );
        }
    }

    private void clearForm() {

        amountField.setText("");
        referenceField.setText("");

        billIdField.setText("");

        currentBillId = -1;

        resetBillDetails();

        tableModel.setRowCount(0);
    }

    private void resetBillDetails() {

        patientLabel.setText("-");
        appointmentLabel.setText("-");
        totalLabel.setText("₹ 0.00");
        paidLabel.setText("₹ 0.00");
        balanceLabel.setText("₹ 0.00");
        statusLabel.setText("-");

        totalAmount = 0;
        paidAmount = 0;
        currentBillId = -1;
    }
}