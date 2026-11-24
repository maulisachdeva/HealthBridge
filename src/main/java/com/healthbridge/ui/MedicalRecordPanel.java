package com.healthbridge.ui;

import com.healthbridge.dao.MedicalRecordDAO;
import com.healthbridge.model.MedicalRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class MedicalRecordPanel extends JPanel {

    private final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);

    private MedicalRecordDAO medicalRecordDAO;

    private JTextField patientIdField;
    private JTextField doctorIdField;
    private JTextField appointmentIdField;
    private JTextField recordDateField;

    private JTextArea symptomsArea;
    private JTextArea diagnosisArea;
    private JTextArea treatmentArea;

    private JTable recordTable;
    private DefaultTableModel tableModel;

    public MedicalRecordPanel() {

        medicalRecordDAO = new MedicalRecordDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);

        JLabel heading = new JLabel("Medical Records");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(PRIMARY_COLOR);
        heading.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        add(heading, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND_COLOR);

        centerPanel.add(createFormPanel(), BorderLayout.NORTH);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadAllRecords();
    }

    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Add Medical Record"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        patientIdField = new JTextField(12);
        doctorIdField = new JTextField(12);
        appointmentIdField = new JTextField(12);
        recordDateField = new JTextField(12);

        symptomsArea = new JTextArea(3, 15);
        diagnosisArea = new JTextArea(3, 15);
        treatmentArea = new JTextArea(3, 15);

        symptomsArea.setLineWrap(true);
        diagnosisArea.setLineWrap(true);
        treatmentArea.setLineWrap(true);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Patient ID:"), gbc);

        gbc.gridx = 1;
        panel.add(patientIdField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Doctor ID:"), gbc);

        gbc.gridx = 3;
        panel.add(doctorIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Appointment ID:"), gbc);

        gbc.gridx = 1;
        panel.add(appointmentIdField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Record Date (yyyy-mm-dd):"), gbc);

        gbc.gridx = 3;
        panel.add(recordDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Symptoms:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(new JScrollPane(symptomsArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Diagnosis:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(new JScrollPane(diagnosisArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Treatment:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(new JScrollPane(treatmentArea), gbc);

        JButton saveButton = new JButton("Save Record");
        JButton searchButton = new JButton("Search");
        JButton viewButton = new JButton("View All Records");
        JButton clearButton = new JButton("Clear");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        buttonPanel.add(saveButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);

        saveButton.addActionListener(e -> saveRecord());
        searchButton.addActionListener(e -> searchRecords());

        viewButton.addActionListener(e -> {

            loadAllRecords();
        });

        clearButton.addActionListener(e -> clearForm());

        return panel;
    }

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Medical History"));

        String[] columns = {
                "Record ID", "Patient ID", "Doctor ID",
                "Appointment ID", "Symptoms", "Diagnosis",
                "Treatment", "Record Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recordTable = new JTable(tableModel);
        recordTable.setRowHeight(25);
        recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(recordTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void saveRecord() {

        try {
            int patientId = Integer.parseInt(patientIdField.getText().trim());
            int doctorId = Integer.parseInt(doctorIdField.getText().trim());
            int appointmentId = Integer.parseInt(appointmentIdField.getText().trim());

            Date recordDate = Date.valueOf(recordDateField.getText().trim());

            String symptoms = symptomsArea.getText().trim();
            String diagnosis = diagnosisArea.getText().trim();
            String treatment = treatmentArea.getText().trim();

            if (diagnosis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter diagnosis.");
                return;
            }

            MedicalRecord record = new MedicalRecord(
                    0, patientId, doctorId, appointmentId,
                    symptoms, diagnosis, treatment, recordDate
            );

            boolean success = medicalRecordDAO.addMedicalRecord(record);

            if (success) {
                JOptionPane.showMessageDialog(this, "Medical record saved successfully!");
                clearForm();
                loadAllRecords();
            } else {
                JOptionPane.showMessageDialog(this, "Unable to save medical record.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric IDs.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Enter date in yyyy-mm-dd format.");
        }
    }

    private void loadAllRecords() {

        tableModel.setRowCount(0);

        List<MedicalRecord> records =
                medicalRecordDAO.getAllMedicalRecords();

        displayRecords(records);



        tableModel.fireTableDataChanged();
        recordTable.revalidate();
        recordTable.repaint();
    }

    private void searchRecords() {

        try {
            int patientId = Integer.parseInt(patientIdField.getText().trim());

            List<MedicalRecord> records =
                    medicalRecordDAO.getMedicalRecordsByPatientId(patientId);

            tableModel.setRowCount(0);
            displayRecords(records);

            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No medical records found.");
            }

            tableModel.fireTableDataChanged();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Patient ID.");
        }
    }

    private void displayRecords(List<MedicalRecord> records) {

        for (MedicalRecord record : records) {
            tableModel.addRow(new Object[]{
                    record.getRecordId(),
                    record.getPatientId(),
                    record.getDoctorId(),
                    record.getAppointmentId(),
                    record.getSymptoms(),
                    record.getDiagnosis(),
                    record.getTreatment(),
                    record.getRecordDate()
            });
        }
    }

    private void clearForm() {

        patientIdField.setText("");
        doctorIdField.setText("");
        appointmentIdField.setText("");
        recordDateField.setText("");

        symptomsArea.setText("");
        diagnosisArea.setText("");
        treatmentArea.setText("");
    }
}