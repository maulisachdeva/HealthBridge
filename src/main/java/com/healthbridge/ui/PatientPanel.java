
package com.healthbridge.ui;

import com.healthbridge.dao.PatientDAO;
import com.healthbridge.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    private JButton registerButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton searchButton;
    private JButton refreshButton;

    private int selectedPatientId = -1;

    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color HEADING_COLOR = new Color(25, 45, 85);

    public PatientPanel() {

        setLayout(new BorderLayout(12, 12));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel heading = new JLabel("Patient Management");
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

        // Load patients in a background thread
        loadPatients();
    }

    // Create patient form
    private JPanel createFormPanel() {

        JPanel form = new JPanel(new GridLayout(6, 2, 12, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Patient Details"),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        nameField = new JTextField();
        ageField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();

        genderBox = new JComboBox<>(
                new String[]{"Male", "Female", "Other"}
        );

        addFormField(form, "Name:", nameField);
        addFormField(form, "Age:", ageField);
        addFormField(form, "Gender:", genderBox);
        addFormField(form, "Phone:", phoneField);
        addFormField(form, "Email:", emailField);
        addFormField(form, "Address:", addressField);

        // Action buttons
        registerButton = new JButton("Register Patient");
        updateButton = new JButton("Update Patient");
        deleteButton = new JButton("Delete Patient");
        clearButton = new JButton("Clear");

        registerButton.addActionListener(e -> registerPatient());
        updateButton.addActionListener(e -> updatePatient());
        deleteButton.addActionListener(e -> deletePatient());
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

    // Add a label and its input field
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

    // Create patient table
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Registered Patients"));

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

        patientTable.setFont(new Font("Arial", Font.PLAIN, 12));
        patientTable.setRowHeight(32);

        patientTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(PatientPanel.this::adjustTableRowHeights);
            }
        });

        patientTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        patientTable.setFillsViewportHeight(true);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.getTableHeader().setReorderingAllowed(false);

        patientTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        // Enable text wrapping in table cells
        patientTable.setDefaultRenderer(
                Object.class,
                new WrappedCellRenderer()
        );

        setColumnWidths();

        // Fill the form when a table row is selected
        patientTable.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int selectedRow = patientTable.getSelectedRow();

                if (selectedRow != -1) {
                    fillFormFromTable(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(
                patientTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        // Recalculate row heights when table data changes
        tableModel.addTableModelListener(e ->
                SwingUtilities.invokeLater(this::adjustTableRowHeights)
        );

        // Search controls
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.gridy = 0;

        JLabel searchLabel = new JLabel("Patient ID:");

        searchField = new JTextField(12);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));

        searchButton = new JButton("Search by ID");
        refreshButton = new JButton("Refresh");

        styleButton(searchButton);
        styleButton(refreshButton);

        searchButton.addActionListener(e -> searchPatient());
        refreshButton.addActionListener(e -> loadPatients());

        searchField.addActionListener(e -> searchPatient());

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

    // Set suitable widths for table columns
    private void setColumnWidths() {

        int[] widths = {60, 150, 60, 90, 120, 190, 250};

        for (int i = 0; i < widths.length; i++) {

            TableColumn column = patientTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
            column.setMinWidth(50);
        }
    }

    // Adjust row height according to wrapped text
    private void adjustTableRowHeights() {

        if (patientTable == null || !patientTable.isShowing()) {
            return;
        }

        for (int row = 0; row < patientTable.getRowCount(); row++) {

            int rowHeight = 32;

            for (int column = 0;
                 column < patientTable.getColumnCount();
                 column++) {

                Component component = patientTable.prepareRenderer(
                        patientTable.getCellRenderer(row, column),
                        row,
                        column
                );

                int preferredHeight = component.getPreferredSize().height;

                rowHeight = Math.max(rowHeight, preferredHeight + 6);
            }

            // Prevent unusually long text from making a row enormous
            rowHeight = Math.min(rowHeight, 120);

            patientTable.setRowHeight(row, rowHeight);
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

    // Disable buttons while a database operation is running
    private void setBusy(boolean busy) {

        registerButton.setEnabled(!busy);
        updateButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);

        searchButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
    }

    // Handle exceptions from background workers
    private void showWorkerError(String operation, Exception e) {

        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }

        Throwable cause = e;

        if (e instanceof ExecutionException && e.getCause() != null) {
            cause = e.getCause();
        }

        JOptionPane.showMessageDialog(
                this,
                operation + " failed: " + cause.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // Register patient using SwingWorker
    private void registerPatient() {

        Patient patient;

        try {
            patient = getPatientFromForm(0);

            if (patient == null) {
                return;
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid age.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        setBusy(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {

                // Database operation runs in background thread
                return patientDAO.addPatient(patient);
            }

            @Override
            protected void done() {

                // UI updates happen on Swing EDT
                setBusy(false);

                try {

                    boolean result = get();

                    if (result) {

                        JOptionPane.showMessageDialog(
                                PatientPanel.this,
                                "Patient registered successfully!"
                        );

                        clearForm();
                        loadPatients();

                    } else {

                        JOptionPane.showMessageDialog(
                                PatientPanel.this,
                                "Registration failed! Check for duplicate details."
                        );
                    }

                } catch (InterruptedException | ExecutionException e) {
                    showWorkerError("Patient registration", e);
                }
            }
        };

        worker.execute();
    }

    // Update selected patient using SwingWorker
    private void updatePatient() {

        if (selectedPatientId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a patient from the table first!"
            );

            return;
        }

        Patient patient;

        try {

            patient = getPatientFromForm(selectedPatientId);

            if (patient == null) {
                return;
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid age.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        setBusy(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {

                return patientDAO.updatePatient(patient);
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    boolean result = get();

                    if (result) {

                        JOptionPane.showMessageDialog(
                                PatientPanel.this,
                                "Patient updated successfully!"
                        );

                        clearForm();
                        loadPatients();

                    } else {

                        JOptionPane.showMessageDialog(
                                PatientPanel.this,
                                "Patient update failed!"
                        );
                    }

                } catch (InterruptedException | ExecutionException e) {
                    showWorkerError("Patient update", e);
                }
            }
        };

        worker.execute();
    }

    // Delete selected patient using SwingWorker
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

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // Store ID before starting the background operation
        int patientId = selectedPatientId;

        setBusy(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {

                return patientDAO.deletePatient(patientId);
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    boolean result = get();

                    if (result) {

                        JOptionPane.showMessageDialog(
                                PatientPanel.this,
                                "Patient deleted successfully!"
                        );

                        clearForm();
                        loadPatients();

                    } else {

                        JOptionPane.showMessageDialog(
                                PatientPanel.this,
                                "Patient deletion failed!"
                        );
                    }

                } catch (InterruptedException | ExecutionException e) {
                    showWorkerError("Patient deletion", e);
                }
            }
        };

        worker.execute();
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

    // Load all patients using SwingWorker
    private void loadPatients() {

        setBusy(true);

        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Patient> doInBackground() {

                // Fetch patients without blocking the GUI
                return patientDAO.getAllPatients();
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    List<Patient> patients = get();

                    tableModel.setRowCount(0);

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

                    SwingUtilities.invokeLater(
                            PatientPanel.this::adjustTableRowHeights
                    );

                } catch (InterruptedException | ExecutionException e) {
                    showWorkerError("Loading patients", e);
                }
            }
        };

        worker.execute();
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

    // Search patient by ID using SwingWorker
    private void searchPatient() {

        int patientId;

        try {

            patientId = Integer.parseInt(
                    searchField.getText().trim()
            );

            if (patientId <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid patient ID!"
            );

            return;
        }

        setBusy(true);

        SwingWorker<Patient, Void> worker = new SwingWorker<>() {

            @Override
            protected Patient doInBackground() {

                return patientDAO.getPatientById(patientId);
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    Patient patient = get();

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
                                PatientPanel.this,
                                "Patient not found!"
                        );
                    }

                    adjustTableRowHeights();

                } catch (InterruptedException | ExecutionException e) {
                    showWorkerError("Patient search", e);
                }
            }
        };

        worker.execute();
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
