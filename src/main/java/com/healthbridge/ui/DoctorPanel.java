
package com.healthbridge.ui;

import com.healthbridge.dao.DoctorDAO;
import com.healthbridge.model.Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DoctorPanel extends JPanel {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    private JTextField nameField;
    private JTextField specializationField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField searchField;

    private JTable doctorTable;
    private DefaultTableModel tableModel;

    private int selectedDoctorId = -1;

    public DoctorPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));

        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = new JLabel("Doctor Management");
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

        loadDoctors();
    }

    // Create doctor form
    private JPanel createFormPanel() {

        JPanel form = new JPanel(new GridLayout(2, 4, 10, 12));

        form.setBackground(Color.WHITE);
        form.setBorder(
                BorderFactory.createTitledBorder("Doctor Details")
        );

        nameField = new JTextField();
        specializationField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();

        form.add(new JLabel("Doctor Name:"));
        form.add(nameField);

        form.add(new JLabel("Specialization:"));
        form.add(specializationField);

        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        form.add(new JLabel("Email:"));
        form.add(emailField);

        JButton registerButton = new JButton("Register Doctor");
        JButton updateButton = new JButton("Update Doctor");
        JButton deleteButton = new JButton("Delete Doctor");
        JButton clearButton = new JButton("Clear");

        registerButton.addActionListener(e -> registerDoctor());
        updateButton.addActionListener(e -> updateDoctor());
        deleteButton.addActionListener(e -> deleteDoctor());
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

    // Create doctor table
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        panel.setBackground(Color.WHITE);
        panel.setBorder(
                BorderFactory.createTitledBorder("Registered Doctors")
        );

        String[] columns = {
                "ID", "Name", "Specialization", "Phone", "Email"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        doctorTable = new JTable(tableModel);

        // Fill form when a row is selected
        doctorTable.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int selectedRow = doctorTable.getSelectedRow();

                if (selectedRow != -1) {
                    fillFormFromTable(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(doctorTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField(12);

        JButton searchButton = new JButton("Search by ID");
        JButton refreshButton = new JButton("Refresh");

        searchButton.addActionListener(e -> searchDoctor());
        refreshButton.addActionListener(e -> loadDoctors());

        searchPanel.add(new JLabel("Doctor ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Register doctor
    private void registerDoctor() {

        try {

            Doctor doctor = getDoctorFromForm(0);

            if (doctor == null) {
                return;
            }

            boolean result = doctorDAO.addDoctor(doctor);

            if (result) {

                JOptionPane.showMessageDialog(
                        this,
                        "Doctor registered successfully!"
                );

                clearForm();
                loadDoctors();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Registration failed! Check duplicate details."
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }

    // Update doctor
    private void updateDoctor() {

        if (selectedDoctorId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a doctor from the table first!"
            );

            return;
        }

        try {

            Doctor doctor = getDoctorFromForm(selectedDoctorId);

            if (doctor == null) {
                return;
            }

            boolean result = doctorDAO.updateDoctor(doctor);

            if (result) {

                JOptionPane.showMessageDialog(
                        this,
                        "Doctor updated successfully!"
                );

                clearForm();
                loadDoctors();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Doctor update failed!"
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
            );
        }
    }

    // Delete doctor
    private void deleteDoctor() {

        if (selectedDoctorId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a doctor from the table first!"
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this doctor?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {

            boolean result = doctorDAO.deleteDoctor(selectedDoctorId);

            if (result) {

                JOptionPane.showMessageDialog(
                        this,
                        "Doctor deleted successfully!"
                );

                clearForm();
                loadDoctors();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Doctor deletion failed!"
                );
            }
        }
    }

    // Get doctor details from form
    private Doctor getDoctorFromForm(int doctorId) {

        String name = nameField.getText().trim();
        String specialization = specializationField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || specialization.isEmpty()
                || phone.isEmpty() || email.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "All fields are required!"
            );

            return null;
        }

        return new Doctor(
                doctorId,
                name,
                specialization,
                phone,
                email
        );
    }

    // Load doctors from database
    private void loadDoctors() {

        tableModel.setRowCount(0);

        List<Doctor> doctors = doctorDAO.getAllDoctors();

        for (Doctor doctor : doctors) {

            tableModel.addRow(new Object[]{
                    doctor.getDoctorId(),
                    doctor.getName(),
                    doctor.getSpecialization(),
                    doctor.getPhone(),
                    doctor.getEmail()
            });
        }
    }

    // Fill form from selected table row
    private void fillFormFromTable(int row) {

        selectedDoctorId = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString()
        );

        nameField.setText(
                tableModel.getValueAt(row, 1).toString()
        );

        specializationField.setText(
                tableModel.getValueAt(row, 2).toString()
        );

        phoneField.setText(
                tableModel.getValueAt(row, 3).toString()
        );

        emailField.setText(
                tableModel.getValueAt(row, 4).toString()
        );
    }

    // Search doctor by ID
    private void searchDoctor() {

        try {

            int doctorId = Integer.parseInt(
                    searchField.getText().trim()
            );

            Doctor doctor = doctorDAO.getDoctorById(doctorId);

            tableModel.setRowCount(0);

            if (doctor != null) {

                tableModel.addRow(new Object[]{
                        doctor.getDoctorId(),
                        doctor.getName(),
                        doctor.getSpecialization(),
                        doctor.getPhone(),
                        doctor.getEmail()
                });

                doctorTable.setRowSelectionInterval(0, 0);

            } else {

                clearForm();

                JOptionPane.showMessageDialog(
                        this,
                        "Doctor not found!"
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid doctor ID!"
            );
        }
    }

    // Clear form
    private void clearForm() {

        selectedDoctorId = -1;

        nameField.setText("");
        specializationField.setText("");
        phoneField.setText("");
        emailField.setText("");

        if (doctorTable != null) {
            doctorTable.clearSelection();
        }
    }
}
