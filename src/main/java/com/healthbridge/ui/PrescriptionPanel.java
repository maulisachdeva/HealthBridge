
package com.healthbridge.ui;

import com.healthbridge.dao.PrescriptionDAO;
import com.healthbridge.model.Prescription;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PrescriptionPanel extends JPanel {

    private final Color DARK_BLUE = new Color(25, 45, 85);
    private final Color BACKGROUND = new Color(245, 247, 250);

    private JTextField recordIdField;
    private JTextField medicineField;
    private JTextField dosageField;
    private JTextField frequencyField;
    private JTextField durationField;

    private JTextArea instructionsArea;

    private JTable prescriptionTable;
    private DefaultTableModel tableModel;

    private final PrescriptionDAO prescriptionDAO =
            new PrescriptionDAO();

    public PrescriptionPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);

        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Heading
        JLabel heading = new JLabel("Prescription Management");

        heading.setFont(new Font("Arial", Font.BOLD, 27));
        heading.setForeground(DARK_BLUE);

        add(heading, BorderLayout.NORTH);

        // Main section
        JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
        centerPanel.setBackground(BACKGROUND);

        centerPanel.add(createForm(), BorderLayout.NORTH);
        centerPanel.add(createHistoryTable(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadAllPrescriptions();
    }

    // ================= FORM =================

    private JPanel createForm() {

        JPanel form = new JPanel(new GridBagLayout());

        form.setBackground(Color.WHITE);

        form.setBorder(
                BorderFactory.createTitledBorder(
                        "Prescription Details"
                )
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        recordIdField = new JTextField(15);
        medicineField = new JTextField(15);
        dosageField = new JTextField(15);
        frequencyField = new JTextField(15);
        durationField = new JTextField(15);

        instructionsArea = new JTextArea(3, 20);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);

        // Row 1
        addField(form, gbc, 0, 0, "Medical Record ID:", recordIdField);
        addField(form, gbc, 2, 0, "Medicine Name:", medicineField);

        // Row 2
        addField(form, gbc, 0, 1, "Dosage:", dosageField);
        addField(form, gbc, 2, 1, "Frequency:", frequencyField);

        // Row 3
        addField(form, gbc, 0, 2, "Duration:", durationField);

        // Instructions
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        form.add(new JLabel("Instructions:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;

        form.add(new JScrollPane(instructionsArea), gbc);

        // Buttons
        JButton saveButton = new JButton("Save Prescription");
        JButton searchButton = new JButton("Search by Record ID");
        JButton viewButton = new JButton("View All Prescriptions");
        JButton clearButton = new JButton("Clear");

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5)
        );

        buttons.setBackground(Color.WHITE);

        buttons.add(saveButton);
        buttons.add(searchButton);
        buttons.add(viewButton);
        buttons.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1;

        form.add(buttons, gbc);

        // Actions
        saveButton.addActionListener(e -> savePrescription());

        searchButton.addActionListener(e -> searchPrescription());

        viewButton.addActionListener(e -> loadAllPrescriptions());

        clearButton.addActionListener(e -> clearForm());

        return form;
    }

    // Helper method for form fields
    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int x,
            int y,
            String label,
            JTextField field) {

        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1;

        panel.add(field, gbc);
    }

    // ================= HISTORY TABLE =================

    private JPanel createHistoryTable() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Prescription History"
                )
        );

        String[] columns = {
                "Prescription ID",
                "Record ID",
                "Medicine",
                "Dosage",
                "Frequency",
                "Duration",
                "Instructions"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        prescriptionTable = new JTable(tableModel);

        prescriptionTable.setRowHeight(28);

        panel.add(
                new JScrollPane(prescriptionTable),
                BorderLayout.CENTER
        );

        return panel;
    }

    // ================= SAVE PRESCRIPTION =================

    private void savePrescription() {

        try {

            // Validate Record ID
            String recordIdText = recordIdField.getText().trim();

            if (recordIdText.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter Medical Record ID."
                );

                return;
            }

            int recordId = Integer.parseInt(recordIdText);

            if (recordId <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Medical Record ID must be positive."
                );

                return;
            }

            String medicine = medicineField.getText().trim();
            String dosage = dosageField.getText().trim();
            String frequency = frequencyField.getText().trim();
            String duration = durationField.getText().trim();
            String instructions = instructionsArea.getText().trim();

            // Validate required fields
            if (medicine.isEmpty() ||
                    dosage.isEmpty() ||
                    frequency.isEmpty() ||
                    duration.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please fill all required fields!"
                );

                return;
            }

            // Validate field lengths
            if (medicine.length() > 100 ||
                    dosage.length() > 100 ||
                    frequency.length() > 100 ||
                    duration.length() > 100 ||
                    instructions.length() > 500) {

                JOptionPane.showMessageDialog(
                        this,
                        "One or more fields exceed the allowed length."
                );

                return;
            }

            Prescription prescription = new Prescription(
                    0,
                    recordId,
                    medicine,
                    dosage,
                    frequency,
                    duration,
                    instructions
            );

            boolean success =
                    prescriptionDAO.addPrescription(prescription);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Prescription saved successfully!"
                );

                clearForm();

                loadAllPrescriptions();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to save prescription. Check whether the Medical Record ID exists."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid numeric Medical Record ID!"
            );
        }
    }

    // ================= SEARCH =================

    private void searchPrescription() {

        try {

            String recordIdText = recordIdField.getText().trim();

            if (recordIdText.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter Medical Record ID."
                );

                return;
            }

            int recordId = Integer.parseInt(recordIdText);

            if (recordId <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Medical Record ID must be positive."
                );

                return;
            }

            List<Prescription> prescriptions =
                    prescriptionDAO.getPrescriptionsByRecordId(recordId);

            displayPrescriptions(prescriptions);

            if (prescriptions.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "No prescriptions found for this Medical Record ID."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid Medical Record ID!"
            );
        }
    }

    // ================= LOAD ALL =================

    private void loadAllPrescriptions() {

        List<Prescription> prescriptions =
                prescriptionDAO.getAllPrescriptions();

        displayPrescriptions(prescriptions);
    }

    // ================= DISPLAY TABLE =================

    private void displayPrescriptions(
            List<Prescription> prescriptions) {

        tableModel.setRowCount(0);

        for (Prescription p : prescriptions) {

            Object[] row = {

                    p.getPrescriptionId(),
                    p.getRecordId(),
                    p.getMedicineName(),
                    p.getDosage(),
                    p.getFrequency(),
                    p.getDuration(),
                    p.getInstructions()
            };

            tableModel.addRow(row);
        }
    }

    // ================= CLEAR =================

    private void clearForm() {

        recordIdField.setText("");
        medicineField.setText("");
        dosageField.setText("");
        frequencyField.setText("");
        durationField.setText("");
        instructionsArea.setText("");
    }
}
