
package com.healthbridge.ui;

import com.healthbridge.dao.PatientDAO;
import com.healthbridge.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PatientPanel extends JPanel {

    private final PatientDAO patientDAO = new PatientDAO();

    private JTextField nameField;
    private JTextField ageField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;

    private JComboBox<String> genderBox;
    private JTextField searchField;

    private JTable patientTable;
    private DefaultTableModel tableModel;

    // Stores the ID of the patient currently selected
    private int selectedPatientId = -1;

    public PatientPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));

        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = new JLabel("Patient Management");
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setForeground(new Color(25, 45, 85));

        add(heading, BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(new Color(245, 247, 250));

        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadPatients();
    }

    // Create patient form
    private JPanel createFormPanel() {

        JPanel form = new JPanel(new GridLayout(3, 4, 10, 12));

        form.setBackground(Color.WHITE);
        form.setBorder(
                BorderFactory.createTitledBorder("Patient Details")
        );

        nameField = new JTextField();
        ageField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();

        genderBox = new JComboBox<>(
                new String[]{"Male", "Female", "Other"}
        );

        form.add(new JLabel("Name:"));
        form.add(nameField);

        form.add(new JLabel("Age:"));
        form.add(ageField);

        form.add(new JLabel("Gender:"));
        form.add(genderBox);

        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        form.add(new JLabel("Email:"));
        form.add(emailField);

        form.add(new JLabel("Address:"));
        form.add(addressField);

        // Form buttons
        JButton registerButton = new JButton("Register Patient");
        JButton updateButton = new JButton("Update Patient");
        JButton deleteButton = new JButton("Delete Patient");
        JButton clearButton = new JButton("Clear");

        registerButton.addActionListener(e -> registerPatient());
        updateButton.addActionListener(e -> updatePatient());
        deleteButton.addActionListener(e -> deletePatient());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10)
        );

        buttonPanel.setBackground(Color.WHITE);

        buttonPanel.add(registerButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);

        wrapper.add(form, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    // Create patient table
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder("Registered Patients")
        );

        String[] columns = {
                "ID", "Name", "Age", "Gender",
                "Phone", "Email", "Address"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);

        // When a row is clicked, fill the form
        patientTable.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int selectedRow = patientTable.getSelectedRow();

                if (selectedRow != -1) {
                    fillFormFromTable(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(patientTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Search controls
        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField(12);

        JButton searchButton = new JButton("Search by ID");
        JButton refreshButton = new JButton("Refresh");

        searchButton.addActionListener(e -> searchPatient());
        refreshButton.addActionListener(e -> loadPatients());

        searchPanel.add(new JLabel("Patient ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Register patient
    private void registerPatient() {

        try {

            Patient patient = getPatientFromForm(0);

            if (patient == null) {
                return;
            }

            boolean result = patientDAO.addPatient(patient);

            if (result) {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient registered successfully!"
                );

                clearForm();
                loadPatients();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Registration failed! Check for duplicate details."
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }

    // Update selected patient
    private void updatePatient() {

        if (selectedPatientId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a patient from the table first!"
            );

            return;
        }

        try {

            Patient patient = getPatientFromForm(selectedPatientId);

            if (patient == null) {
                return;
            }

            boolean result = patientDAO.updatePatient(patient);

            if (result) {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient updated successfully!"
                );

                clearForm();
                loadPatients();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient update failed!"
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }

    // Delete selected patient
    private void deletePatient() {

        if (selectedPatientId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a patient from the table first!"
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this patient?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {

            boolean result = patientDAO.deletePatient(selectedPatientId);

            if (result) {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient deleted successfully!"
                );

                clearForm();
                loadPatients();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Patient deletion failed!"
                );
            }
        }
    }

    // Read patient details from the form
    private Patient getPatientFromForm(int patientId) {

        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || ageText.isEmpty() || phone.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Name, age and phone are required!"
            );

            return null;
        }

        int age = Integer.parseInt(ageText);

        if (age <= 0 || age > 120) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid age!"
            );

            return null;
        }

        return new Patient(
                patientId,
                name,
                age,
                genderBox.getSelectedItem().toString(),
                phone,
                email,
                address
        );
    }

    // Load all patients from database
    private void loadPatients() {

        tableModel.setRowCount(0);

        List<Patient> patients = patientDAO.getAllPatients();

        for (Patient patient : patients) {

            tableModel.addRow(new Object[]{
                    patient.getPatientId(),
                    patient.getName(),
                    patient.getAge(),
                    patient.getGender(),
                    patient.getPhone(),
                    patient.getEmail(),
                    patient.getAddress()
            });
        }
    }

    // Fill form when a table row is selected
    private void fillFormFromTable(int row) {

        selectedPatientId = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString()
        );

        nameField.setText(
                tableModel.getValueAt(row, 1).toString()
        );

        ageField.setText(
                tableModel.getValueAt(row, 2).toString()
        );

        genderBox.setSelectedItem(
                tableModel.getValueAt(row, 3).toString()
        );

        phoneField.setText(
                tableModel.getValueAt(row, 4).toString()
        );

        emailField.setText(
                tableModel.getValueAt(row, 5) == null
                        ? ""
                        : tableModel.getValueAt(row, 5).toString()
        );

        addressField.setText(
                tableModel.getValueAt(row, 6) == null
                        ? ""
                        : tableModel.getValueAt(row, 6).toString()
        );
    }

    // Search patient by ID
    private void searchPatient() {

        try {

            int patientId = Integer.parseInt(
                    searchField.getText().trim()
            );

            Patient patient = patientDAO.getPatientById(patientId);

            tableModel.setRowCount(0);

            if (patient != null) {

                tableModel.addRow(new Object[]{
                        patient.getPatientId(),
                        patient.getName(),
                        patient.getAge(),
                        patient.getGender(),
                        patient.getPhone(),
                        patient.getEmail(),
                        patient.getAddress()
                });

                patientTable.setRowSelectionInterval(0, 0);

            } else {

                clearForm();

                JOptionPane.showMessageDialog(
                        this,
                        "Patient not found!"
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid patient ID!"
            );
        }
    }

    // Clear form and selection
    private void clearForm() {

        selectedPatientId = -1;

        nameField.setText("");
        ageField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");

        genderBox.setSelectedIndex(0);

        if (patientTable != null) {
            patientTable.clearSelection();
        }
    }
}
