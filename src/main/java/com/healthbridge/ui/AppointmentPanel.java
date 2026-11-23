
package com.healthbridge.ui;

import com.healthbridge.dao.AppointmentDAO;
import com.healthbridge.model.Appointment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AppointmentPanel extends JPanel {

    private final Color DARK_BLUE = new Color(25, 45, 85);
    private final Color BACKGROUND = new Color(245, 247, 250);

    public AppointmentPanel() {

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = new JLabel("Appointment Management");
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(DARK_BLUE);

        JButton bookButton = new JButton("Book Appointment");
        JButton viewButton = new JButton("View Appointments");
        JButton queueButton = new JButton("Smart Queue");
        JButton cancelButton = new JButton("Cancel Appointment");

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 15, 20)
        );
        buttonPanel.setOpaque(false);

        buttonPanel.add(bookButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(queueButton);
        buttonPanel.add(cancelButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(heading, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        bookButton.addActionListener(e -> showBookingForm());
        viewButton.addActionListener(e -> showAppointments());
        queueButton.addActionListener(e -> showSmartQueue());
        cancelButton.addActionListener(e -> cancelAppointment());
    }

    private void showBookingForm() {

        JTextField patientField = new JTextField(15);
        JTextField doctorField = new JTextField(15);
        JTextField dateField = new JTextField(
                LocalDate.now().toString(), 15
        );
        JTextField timeField = new JTextField(15);
        JTextField reasonField = new JTextField(15);

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{"SCHEDULED", "WALK_IN"}
        );

        JComboBox<String> priorityBox = new JComboBox<>(
                new String[]{"NORMAL", "EMERGENCY"}
        );

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));

        form.add(new JLabel("Patient ID:"));
        form.add(patientField);

        form.add(new JLabel("Doctor ID:"));
        form.add(doctorField);

        form.add(new JLabel("Date (YYYY-MM-DD):"));
        form.add(dateField);

        form.add(new JLabel("Time (HH:MM):"));
        form.add(timeField);

        form.add(new JLabel("Appointment Type:"));
        form.add(typeBox);

        form.add(new JLabel("Priority:"));
        form.add(priorityBox);

        form.add(new JLabel("Reason:"));
        form.add(reasonField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Book Appointment",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int patientId = Integer.parseInt(
                    patientField.getText().trim()
            );

            int doctorId = Integer.parseInt(
                    doctorField.getText().trim()
            );

            LocalDate date = LocalDate.parse(
                    dateField.getText().trim()
            );

            String timeText = timeField.getText().trim();

            LocalTime time = timeText.isEmpty()
                    ? null
                    : LocalTime.parse(timeText);

            if (patientId <= 0 || doctorId <= 0) {
                throw new IllegalArgumentException(
                        "Patient ID and Doctor ID must be positive."
                );
            }

            Appointment appointment = new Appointment();

            appointment.setPatientId(patientId);
            appointment.setDoctorId(doctorId);
            appointment.setAppointmentDate(date);
            appointment.setAppointmentTime(time);
            appointment.setAppointmentType(
                    (String) typeBox.getSelectedItem()
            );
            appointment.setPriority(
                    (String) priorityBox.getSelectedItem()
            );
            appointment.setReason(reasonField.getText().trim());
            appointment.setStatus("WAITING");

            AppointmentDAO dao = new AppointmentDAO();

            boolean saved = dao.addAppointment(appointment);

            if (saved) {
                JOptionPane.showMessageDialog(
                        this,
                        "Appointment booked successfully!\n"
                                + "Appointment ID: "
                                + appointment.getAppointmentId()
                                + "\nToken Number: "
                                + appointment.getTokenNumber(),
                        "Booking Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to book appointment.",
                        "Booking Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {
            showError("Patient ID and Doctor ID must be valid numbers.");

        } catch (DateTimeParseException ex) {
            showError("Enter date as YYYY-MM-DD and time as HH:MM.");

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());

        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showAppointments() {

        try {
            AppointmentDAO dao = new AppointmentDAO();

            List<Appointment> appointments =
                    dao.getAllAppointments();

            String[] columns = {
                    "ID", "Patient ID", "Doctor ID",
                    "Date", "Time", "Token", "Status"
            };

            DefaultTableModel model = new DefaultTableModel(
                    columns, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Appointment a : appointments) {
                model.addRow(new Object[]{
                        a.getAppointmentId(),
                        a.getPatientId(),
                        a.getDoctorId(),
                        a.getAppointmentDate(),
                        a.getAppointmentTime(),
                        a.getTokenNumber(),
                        a.getStatus()
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(28);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(850, 400));

            JOptionPane.showMessageDialog(
                    this,
                    scrollPane,
                    "All Appointments",
                    JOptionPane.PLAIN_MESSAGE
            );

        } catch (Exception ex) {
            showError("Error loading appointments: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showSmartQueue() {

        JTextField doctorField = new JTextField("1", 10);
        JTextField dateField = new JTextField(
                LocalDate.now().toString(), 10
        );

        JPanel filterPanel = new JPanel(
                new GridLayout(2, 2, 10, 10)
        );

        filterPanel.add(new JLabel("Doctor ID:"));
        filterPanel.add(doctorField);

        filterPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        filterPanel.add(dateField);

        int result = JOptionPane.showConfirmDialog(
                this,
                filterPanel,
                "Smart Queue - Select Doctor and Date",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int doctorId = Integer.parseInt(
                    doctorField.getText().trim()
            );

            LocalDate date = LocalDate.parse(
                    dateField.getText().trim()
            );

            if (doctorId <= 0) {
                throw new IllegalArgumentException(
                        "Doctor ID must be positive."
                );
            }

            AppointmentDAO dao = new AppointmentDAO();

            JDialog dialog = new JDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Smart Queue",
                    Dialog.ModalityType.APPLICATION_MODAL
            );

            dialog.setSize(850, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));

            String[] columns = {
                    "Token", "Appointment ID", "Patient ID",
                    "Type", "Priority", "Status"
            };

            DefaultTableModel model = new DefaultTableModel(
                    columns, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(model);
            table.setRowHeight(28);
            table.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION
            );

            Runnable refreshQueue = () -> {
                try {
                    model.setRowCount(0);

                    List<Appointment> queue =
                            dao.getWaitingQueue(doctorId, date);

                    for (Appointment a : queue) {
                        model.addRow(new Object[]{
                                a.getTokenNumber(),
                                a.getAppointmentId(),
                                a.getPatientId(),
                                a.getAppointmentType(),
                                a.getPriority(),
                                a.getStatus()
                        });
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Error loading queue: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            };

            JButton refreshButton = new JButton("Refresh Queue");
            JButton callButton = new JButton("Call Next Patient");
            JButton completeButton = new JButton("Complete Appointment");

            refreshButton.addActionListener(
                    e -> refreshQueue.run()
            );

            callButton.addActionListener(e -> {
                try {
                    Appointment next =
                            dao.callNextPatient(doctorId, date);

                    if (next == null) {
                        JOptionPane.showMessageDialog(
                                dialog,
                                "No patient can be called right now."
                        );
                        return;
                    }

                    JOptionPane.showMessageDialog(
                            dialog,
                            "Now calling Token: "
                                    + next.getTokenNumber()
                                    + "\nPatient ID: "
                                    + next.getPatientId()
                                    + "\nAppointment ID: "
                                    + next.getAppointmentId()
                    );

                    refreshQueue.run();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Error calling patient: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            });

            completeButton.addActionListener(e -> {

                String input = JOptionPane.showInputDialog(
                        dialog,
                        "Enter the Appointment ID to complete:"
                );

                if (input == null) {
                    return;
                }

                try {
                    int appointmentId = Integer.parseInt(
                            input.trim()
                    );

                    if (appointmentId <= 0) {
                        throw new IllegalArgumentException(
                                "Appointment ID must be positive."
                        );
                    }

                    Appointment appointment =
                            dao.getAppointmentById(appointmentId);

                    if (appointment == null) {
                        JOptionPane.showMessageDialog(
                                dialog,
                                "Appointment not found."
                        );
                        return;
                    }

                    if (appointment.getDoctorId() != doctorId
                            || !date.equals(
                            appointment.getAppointmentDate())) {
                        JOptionPane.showMessageDialog(
                                dialog,
                                "This appointment does not belong "
                                        + "to the selected doctor and date."
                        );
                        return;
                    }

                    if (!"IN_PROGRESS".equals(
                            appointment.getStatus())) {
                        JOptionPane.showMessageDialog(
                                dialog,
                                "Only IN_PROGRESS appointments "
                                        + "can be completed."
                        );
                        return;
                    }

                    dao.completeAppointment(appointmentId);

                    Appointment updated =
                            dao.getAppointmentById(appointmentId);

                    if (updated != null
                            && "COMPLETED".equals(updated.getStatus())) {
                        JOptionPane.showMessageDialog(
                                dialog,
                                "Appointment completed successfully."
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                dialog,
                                "Completion was not confirmed. "
                                        + "Check the appointment status."
                        );
                    }

                    refreshQueue.run();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Enter a valid numeric Appointment ID."
                    );

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Error completing appointment: "
                                    + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            });

            JPanel buttons = new JPanel(new FlowLayout());

            buttons.add(refreshButton);
            buttons.add(callButton);
            buttons.add(completeButton);

            JLabel heading = new JLabel(
                    "Queue for Doctor " + doctorId + " on " + date
            );

            heading.setFont(
                    new Font("Arial", Font.BOLD, 18)
            );

            heading.setBorder(
                    BorderFactory.createEmptyBorder(15, 15, 10, 15)
            );

            dialog.add(heading, BorderLayout.NORTH);
            dialog.add(
                    new JScrollPane(table),
                    BorderLayout.CENTER
            );
            dialog.add(buttons, BorderLayout.SOUTH);

            refreshQueue.run();
            dialog.setVisible(true);

        } catch (NumberFormatException ex) {
            showError("Doctor ID must be a valid number.");

        } catch (DateTimeParseException ex) {
            showError("Enter the date in YYYY-MM-DD format.");

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void cancelAppointment() {

        String input = JOptionPane.showInputDialog(
                this,
                "Enter Appointment ID to cancel:"
        );

        if (input == null) {
            return;
        }

        try {
            int appointmentId = Integer.parseInt(input.trim());

            if (appointmentId <= 0) {
                throw new IllegalArgumentException(
                        "Appointment ID must be positive."
                );
            }

            AppointmentDAO dao = new AppointmentDAO();

            Appointment appointment =
                    dao.getAppointmentById(appointmentId);

            if (appointment == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Appointment not found."
                );
                return;
            }

            if (!"WAITING".equals(appointment.getStatus())) {
                JOptionPane.showMessageDialog(
                        this,
                        "Only WAITING appointments can be cancelled."
                );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Cancel Appointment " + appointmentId + "?",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            dao.cancelAppointment(appointmentId);

            Appointment updated =
                    dao.getAppointmentById(appointmentId);

            if (updated != null
                    && "CANCELLED".equals(updated.getStatus())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Appointment cancelled successfully."
                );

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Cancellation was not confirmed. "
                                + "Check the appointment status."
                );
            }

        } catch (NumberFormatException ex) {
            showError("Enter a valid numeric Appointment ID.");

        } catch (Exception ex) {
            showError("Error cancelling appointment: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
