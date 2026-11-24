
package com.healthbridge.ui;

import com.healthbridge.dao.DoctorDAO;
import com.healthbridge.model.Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
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

    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color HEADING_COLOR = new Color(25, 45, 85);

    public DoctorPanel() {

        setLayout(new BorderLayout(12, 12));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel heading = new JLabel("Doctor Management");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(HEADING_COLOR);

        add(heading, BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setBackground(BACKGROUND);

        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadDoctors();
    }

    // Create doctor form
    private JPanel createFormPanel() {

        JPanel form = new JPanel(new GridLayout(4, 2, 12, 10));

        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Doctor Details"),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        nameField = new JTextField();
        specializationField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();

        addFormField(form, "Doctor Name:", nameField);
        addFormField(form, "Specialization:", specializationField);
        addFormField(form, "Phone:", phoneField);
        addFormField(form, "Email:", emailField);

        // Action buttons
        JButton registerButton = new JButton("Register Doctor");
        JButton updateButton = new JButton("Update Doctor");
        JButton deleteButton = new JButton("Delete Doctor");
        JButton clearButton = new JButton("Clear");

        registerButton.addActionListener(e -> registerDoctor());
        updateButton.addActionListener(e -> updateDoctor());
        deleteButton.addActionListener(e -> deleteDoctor());
        clearButton.addActionListener(e -> clearForm());

        styleButton(registerButton);
        styleButton(updateButton);
        styleButton(deleteButton);
        styleButton(clearButton);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 8));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        buttonPanel.add(registerButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel wrapper = new JPanel(new BorderLayout(5, 5));
        wrapper.setBackground(Color.WHITE);

        wrapper.add(form, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    // Add a label and input field
    private void addFormField(JPanel panel, String labelText, JComponent field) {

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 13));

        field.setFont(new Font("Arial", Font.PLAIN, 13));

        panel.add(label);
        panel.add(field);
    }

    // Style action buttons
    private void styleButton(JButton button) {

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setMargin(new Insets(8, 8, 8, 8));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(button.getText());
    }

    // Create doctor table
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Registered Doctors"));

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

        doctorTable.setFont(new Font("Arial", Font.PLAIN, 12));
        doctorTable.setRowHeight(32);
        doctorTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        doctorTable.setFillsViewportHeight(true);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorTable.getTableHeader().setReorderingAllowed(false);
        doctorTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        // Enable wrapped text
        doctorTable.setDefaultRenderer(
                Object.class,
                new WrappedCellRenderer()
        );

        setColumnWidths();

        // Recalculate row heights when table is resized
        doctorTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(
                        DoctorPanel.this::adjustTableRowHeights
                );
            }
        });

        // Fill form when a row is selected
        doctorTable.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int selectedRow = doctorTable.getSelectedRow();

                if (selectedRow != -1) {
                    fillFormFromTable(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(
                doctorTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        // Search controls
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.gridy = 0;

        JLabel searchLabel = new JLabel("Doctor ID:");

        searchField = new JTextField(12);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));

        JButton searchButton = new JButton("Search by ID");
        JButton refreshButton = new JButton("Refresh");

        styleButton(searchButton);
        styleButton(refreshButton);

        searchButton.addActionListener(e -> searchDoctor());
        refreshButton.addActionListener(e -> loadDoctors());

        searchField.addActionListener(e -> searchDoctor());

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(searchLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(searchField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(searchButton, gbc);

        gbc.gridx = 3;
        searchPanel.add(refreshButton, gbc);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Set suitable initial column widths
    private void setColumnWidths() {

        int[] widths = {60, 180, 220, 140, 220};

        for (int i = 0; i < widths.length; i++) {

            TableColumn column = doctorTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
            column.setMinWidth(55);
        }
    }

    // Adjust row height according to wrapped text
    private void adjustTableRowHeights() {

        if (doctorTable == null || !doctorTable.isShowing()) {
            return;
        }

        for (int row = 0; row < doctorTable.getRowCount(); row++) {

            int rowHeight = 32;

            for (int column = 0;
                 column < doctorTable.getColumnCount();
                 column++) {

                Component component = doctorTable.prepareRenderer(
                        doctorTable.getCellRenderer(row, column),
                        row,
                        column
                );

                int preferredHeight = component.getPreferredSize().height;
                rowHeight = Math.max(rowHeight, preferredHeight + 6);
            }

            // Limit very tall rows
            rowHeight = Math.min(rowHeight, 120);

            doctorTable.setRowHeight(row, rowHeight);
        }
    }

    // Renderer that wraps long text onto multiple lines
    private static class WrappedCellRenderer extends JTextArea
            implements javax.swing.table.TableCellRenderer {

        public WrappedCellRenderer() {

            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(new Font("Arial", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            setText(value == null ? "" : value.toString());

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            int columnWidth = table.getColumnModel()
                    .getColumn(column)
                    .getWidth();

            setSize(columnWidth, Short.MAX_VALUE);

            return this;
        }
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

            try {

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

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Error: " + e.getMessage()
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

        SwingUtilities.invokeLater(this::adjustTableRowHeights);
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
