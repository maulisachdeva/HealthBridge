package com.healthbridge.ui;

import com.healthbridge.dao.BillDAO;
import com.healthbridge.model.Bill;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class GenerateBillPanel extends JPanel {

    private final Color DARK_BLUE = new Color(25, 45, 85);
    private final Color BACKGROUND = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;

    private JTextField patientIdField;
    private JTextField appointmentIdField;

    private JTextField consultationField;
    private JTextField medicineField;
    private JTextField otherField;

    private JLabel totalLabel;

    private BillDAO billDAO;

    private Runnable onSuccess;

    public GenerateBillPanel() {
        this(null); 
    }

    public GenerateBillPanel(Runnable onSuccess) {

        this.onSuccess = onSuccess;

        billDAO = new BillDAO();

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        setBorder(
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
    }

    // Header
    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(BACKGROUND);

        JLabel title = new JLabel("Generate New Bill");

        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Enter patient and billing details"
        );

        subtitle.setForeground(Color.GRAY);

        JPanel text = new JPanel();

        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(BACKGROUND);

        text.add(title);
        text.add(Box.createVerticalStrut(8));
        text.add(subtitle);

        header.add(text, BorderLayout.WEST);

        return header;
    }

    // Form
    private JPanel createForm() {

        JPanel form = new JPanel(new GridBagLayout());

        form.setBackground(WHITE);

        form.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 240)
                        ),
                        BorderFactory.createEmptyBorder(
                                30, 40, 30, 40
                        )
                )
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        patientIdField = new JTextField(20);
        appointmentIdField = new JTextField(20);

        consultationField = new JTextField("0");
        medicineField = new JTextField("0");
        otherField = new JTextField("0");

        addField(form, gbc, 0, "Patient ID", patientIdField);
        addField(form, gbc, 1, "Appointment ID", appointmentIdField);

        addField(form, gbc, 2, "Consultation Charges (₹)", consultationField);
        addField(form, gbc, 3, "Medicine Charges (₹)", medicineField);
        addField(form, gbc, 4, "Other Charges (₹)", otherField);

        totalLabel = new JLabel("₹0.00");

        totalLabel.setFont(
                new Font("Arial", Font.BOLD, 25)
        );

        totalLabel.setForeground(DARK_BLUE);

        gbc.gridx = 0;
        gbc.gridy = 5;

        form.add(new JLabel("Total Amount"), gbc);

        gbc.gridx = 1;

        form.add(totalLabel, gbc);

        JButton generateButton = new JButton("Generate Bill");

        generateButton.setBackground(DARK_BLUE);
        generateButton.setForeground(WHITE);
        generateButton.setFocusPainted(false);

        JButton clearButton = new JButton("Clear");

        clearButton.addActionListener(e -> clearForm());

        generateButton.addActionListener(e -> generateBill());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttons.setBackground(WHITE);

        buttons.add(clearButton);
        buttons.add(generateButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;

        form.add(buttons, gbc);

        // Automatic calculation
        consultationField.getDocument().addDocumentListener(
                new CalculationListener(this)
        );

        medicineField.getDocument().addDocumentListener(
                new CalculationListener(this)
        );

        otherField.getDocument().addDocumentListener(
                new CalculationListener(this)
        );

        return form;
    }

    // Add form field
    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JTextField field) {

        gbc.gridx = 0;
        gbc.gridy = row;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;

        panel.add(field, gbc);
    }

    // Calculate total
    private double calculateTotal() {

        double consultation = Double.parseDouble(
                consultationField.getText().trim()
        );

        double medicine = Double.parseDouble(
                medicineField.getText().trim()
        );

        double other = Double.parseDouble(
                otherField.getText().trim()
        );

        return consultation + medicine + other;
    }

    private void updateTotal() {

        try {

            totalLabel.setText(
                    String.format("₹%.2f", calculateTotal())
            );

        } catch (NumberFormatException e) {

            totalLabel.setText("Invalid Amount");
        }
    }

    // Generate bill
    private void generateBill() {

        try {

            int patientId = Integer.parseInt(
                    patientIdField.getText().trim()
            );

            int appointmentId = Integer.parseInt(
                    appointmentIdField.getText().trim()
            );

            double consultation = Double.parseDouble(
                    consultationField.getText().trim()
            );

            double medicine = Double.parseDouble(
                    medicineField.getText().trim()
            );

            double other = Double.parseDouble(
                    otherField.getText().trim()
            );

            if (patientId <= 0 || appointmentId <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient ID and Appointment ID must be positive.",
                        "Invalid Details",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (consultation < 0 || medicine < 0 || other < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Charges cannot be negative.",
                        "Invalid Amount",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            double total = consultation + medicine + other;

           Bill bill = new Bill(
        patientId,
        appointmentId,
        consultation,
        medicine,
        other,
        LocalDate.now()
);

            bill.setPaymentStatus("PENDING");

            boolean success = billDAO.createBill(bill);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Bill generated successfully!\nTotal: ₹" + total,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                if (onSuccess != null) {
                    onSuccess.run();
                }

                clearForm();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to generate bill. Please check database connection and details.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException e) {

    JOptionPane.showMessageDialog(
            this,
            "Invalid value: " + e.getMessage()
                    + "\n\nPatient ID and Appointment ID must be integers."
                    + "\nCharges must contain only numbers.",
            "Invalid Input",
            JOptionPane.WARNING_MESSAGE
    );

}catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Clear fields
    private void clearForm() {

        patientIdField.setText("");
        appointmentIdField.setText("");

        consultationField.setText("0");
        medicineField.setText("0");
        otherField.setText("0");

        totalLabel.setText("₹0.00");
    }

    // Document listener for automatic calculation
    private static class CalculationListener
            implements javax.swing.event.DocumentListener {

        private GenerateBillPanel panel;

        CalculationListener(GenerateBillPanel panel) {
            this.panel = panel;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            panel.updateTotal();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            panel.updateTotal();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            panel.updateTotal();
        }
    }
}