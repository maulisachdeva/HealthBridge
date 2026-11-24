package com.healthbridge.ui;

import com.healthbridge.dao.MedicalRecordDAO;
import com.healthbridge.model.MedicalRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class MedicalRecordPanel extends JPanel {

    private final Color DARK_BLUE = new Color(25, 45, 85);
    private final Color BACKGROUND = new Color(245, 247, 250);

    private final MedicalRecordDAO medicalRecordDAO =
            new MedicalRecordDAO();

    private JTextField patientIdField;
    private JTextField doctorIdField;
    private JTextField appointmentIdField;
    private JTextField dateField;

    private JTextArea symptomsArea;
    private JTextArea diagnosisArea;
    private JTextArea treatmentArea;

    private JTable recordTable;
    private DefaultTableModel tableModel;

    public MedicalRecordPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);

        setBorder(
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        );

        JLabel heading = new JLabel("Medical Records Management");

        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(DARK_BLUE);

        add(heading, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND);

        centerPanel.add(createFormPanel(), BorderLayout.NORTH);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadAllRecords();
    }

    // Create medical record form
    private JPanel createFormPanel() {

        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBackground(Color.WHITE);

        wrapper.setBorder(
                BorderFactory.createTitledBorder("Medical Record Details")
        );

        JPanel fields = new JPanel(new GridLayout(2, 4, 10, 8));
        fields.setBackground(Color.WHITE);

        patientIdField = new JTextField();
        doctorIdField = new JTextField();
        appointmentIdField = new JTextField();
        dateField = new JTextField();

        dateField.setText(
                new java.text.SimpleDateFormat("yyyy-MM-dd")
                        .format(new java.util.Date())
        );

        fields.add(new JLabel("Patient ID:"));
        fields.add(new JLabel("Doctor ID:"));
        fields.add(new JLabel("Appointment ID:"));
        fields.add(new JLabel("Record Date (YYYY-MM-DD):"));

        fields.add(patientIdField);
        fields.add(doctorIdField);
        fields.add(appointmentIdField);
        fields.add(dateField);

        wrapper.add(fields, BorderLayout.NORTH);

        JPanel textPanel = new JPanel(new GridLayout(1, 3, 10, 5));
        textPanel.setBackground(Color.WHITE);

        symptomsArea = new JTextArea(4, 15);
        diagnosisArea = new JTextArea(4, 15);
        treatmentArea = new JTextArea(4, 15);

        textPanel.add(createTextSection("Symptoms", symptomsArea));
        textPanel.add(createTextSection("Diagnosis", diagnosisArea));
        textPanel.add(createTextSection("Treatment", treatmentArea));

        wrapper.add(textPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Record");
        JButton searchButton = new JButton("Search by Patient ID");
        JButton viewButton = new JButton("View All Records");
        JButton clearButton = new JButton("Clear");

        saveButton.addActionListener(e -> saveRecord());

        searchButton.addActionListener(e -> searchRecords());

        viewButton.addActionListener(e -> loadAllRecords());

        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5)
        );

        buttons.setBackground(Color.WHITE);

        buttons.add(saveButton);
        buttons.add(searchButton);
        buttons.add(viewButton);
        buttons.add(clearButton);

        wrapper.add(buttons, BorderLayout.SOUTH);

        return wrapper;
    }

    // Create text area section
    private JPanel createTextSection(String title, JTextArea area) {

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel(title), BorderLayout.NORTH);

        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        return panel;
    }

    // Create medical records table
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder("Medical History")
        );

        String[] columns = {
                "Record ID",
                "Patient ID",
                "Doctor ID",
                "Appointment ID",
                "Symptoms",
                "Diagnosis",
                "Treatment",
                "Record Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recordTable = new JTable(tableModel);

        recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(recordTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Save medical record
    private void saveRecord() {

        try {

            int patientId = Integer.parseInt(
                    patientIdField.getText().trim()
            );

            int doctorId = Integer.parseInt(
                    doctorIdField.getText().trim()
            );

            int appointmentId = Integer.parseInt(
                    appointmentIdField.getText().trim()
            );

            String symptoms = symptomsArea.getText().trim();
            String diagnosis = diagnosisArea.getText().trim();
            String treatment = treatmentArea.getText().trim();

            if (diagnosis.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Diagnosis is required!"
                );

                return;
            }

            Date recordDate = Date.valueOf(
                    dateField.getText().trim()
            );

            MedicalRecord record = new MedicalRecord(
                    0,
                    patientId,
                    doctorId,
                    appointmentId,
                    symptoms,
                    diagnosis,
                    treatment,
                    recordDate
            );

            boolean result =
                    medicalRecordDAO.addMedicalRecord(record);

            if (result) {

                JOptionPane.showMessageDialog(
                        this,
                        "Medical record saved successfully!"
                );

                clearForm();
                loadAllRecords();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to save medical record!"
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid Patient, Doctor and Appointment IDs!"
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter date in YYYY-MM-DD format!"
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }

    // Load all medical records
    private void loadAllRecords() {

        tableModel.setRowCount(0);

        List<MedicalRecord> records =
                medicalRecordDAO.getAllMedicalRecords();

        displayRecords(records);
    }

    // Search records using patient ID
    private void searchRecords() {

        try {

            int patientId = Integer.parseInt(
                    patientIdField.getText().trim()
            );

            List<MedicalRecord> records =
                    medicalRecordDAO.getMedicalRecordsByPatientId(
                            patientId
                    );

            tableModel.setRowCount(0);

            displayRecords(records);

            if (records.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "No medical records found!"
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid Patient ID!"
            );
        }
    }

    // Display records in JTable
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

    // Clear form
    private void clearForm() {

        patientIdField.setText("");
        doctorIdField.setText("");
        appointmentIdField.setText("");
        dateField.setText(
                new java.text.SimpleDateFormat("yyyy-MM-dd")
                        .format(new java.util.Date())
        );

        symptomsArea.setText("");
        diagnosisArea.setText("");
        treatmentArea.setText("");
    }
}
