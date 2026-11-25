
package com.healthbridge.ui;

import com.healthbridge.dao.AppointmentDAO;
import com.healthbridge.dao.DoctorDAO;
import com.healthbridge.model.Doctor;
import com.healthbridge.model.Appointment;
import com.healthbridge.service.QueueManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class QueuePanel extends JPanel {

    private final QueueManager manager = new QueueManager();
    private final AppointmentDAO dao = new AppointmentDAO();

    private final DefaultTableModel model =
            new DefaultTableModel(
                    new String[]{
                            "Token",
                            "Appointment ID",
                            "Patient",
                            "Doctor",
                            "Type",
                            "Priority",
                            "Status"
                    }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JLabel currentServingLabel =
            new JLabel("No patient currently being served.");

    private int doctorId;
    private LocalDate date;


    // Constructor
    public QueuePanel() {
        setLayout(new BorderLayout(10, 10));
    }


    // Open Smart Queue
    public void open(Component parent) {

        List<Doctor> doctors = new DoctorDAO().getAllDoctors();

        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "No doctors available."
            );
            return;
        }

        JComboBox<String> doctor = new JComboBox<>();

        for (Doctor d : doctors) {
            doctor.addItem(
                    d.getDoctorId() + " - " + d.getName()
            );
        }

        JTextField day =
                new JTextField(LocalDate.now().toString());

        JPanel filter = new JPanel(
                new GridLayout(2, 2, 10, 10)
        );

        filter.add(new JLabel("Doctor:"));
        filter.add(doctor);

        filter.add(new JLabel("Date (YYYY-MM-DD):"));
        filter.add(day);

        if (JOptionPane.showConfirmDialog(
                parent,
                filter,
                "Select Queue",
                JOptionPane.OK_CANCEL_OPTION
        ) != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            doctorId = doctors.get(
                    doctor.getSelectedIndex()
            ).getDoctorId();

            date = LocalDate.parse(day.getText().trim());

            if (doctorId <= 0) {
                throw new IllegalArgumentException(
                        "Invalid doctor ID"
                );
            }

        } catch (Exception ex) {
            error(parent, ex);
            return;
        }


        // Clear previous UI
        removeAll();


        // Heading
        JLabel title = new JLabel(
                "Queue for " +
                        doctors.get(doctor.getSelectedIndex()).getName() +
                        " on " + date
        );

        title.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        title.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        add(title, BorderLayout.NORTH);


        // Currently Serving Section
        JPanel servingPanel = new JPanel(new BorderLayout());

        servingPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Currently Serving"
                )
        );

        currentServingLabel.setFont(
                new Font("Arial", Font.BOLD, 15)
        );

        currentServingLabel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        servingPanel.add(
                currentServingLabel,
                BorderLayout.CENTER
        );


        // Waiting Queue Table
        JTable table = new JTable(model);

        table.setRowHeight(27);

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Waiting Patients"
                )
        );


        // Center section
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        centerPanel.add(
                servingPanel,
                BorderLayout.NORTH
        );

        centerPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        add(centerPanel, BorderLayout.CENTER);


        // Buttons
        JPanel controls = new JPanel(new FlowLayout());

        JButton refresh = new JButton("Refresh Queue");

        JButton call = new JButton("Call Next Patient");

        JButton complete = new JButton("Complete Appointment");

        controls.add(refresh);
        controls.add(call);
        controls.add(complete);

        add(controls, BorderLayout.SOUTH);


        // Smart Queue Dialog
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Smart Queue",
                Dialog.ModalityType.APPLICATION_MODAL
        );


        // Refresh Button
        refresh.addActionListener(e -> refresh(dialog));


        // Call Next Patient Button
        call.addActionListener(e -> {

            try {

                Appointment next =
                        manager.callNextPatient(doctorId, date);

                if (next == null) {

                    JOptionPane.showMessageDialog(
                            dialog,
                            "No waiting patients."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            dialog,
                            "Calling Token " + next.getTokenNumber() +
                                    "\nPatient: " + next.getPatientName() +
                                    "\nDoctor: " + next.getDoctorName() +
                                    "\nAppointment ID: " + next.getAppointmentId()
                    );
                }

                refresh(dialog);

            } catch (Exception ex) {
                error(dialog, ex);
            }
        });


        // Complete Appointment Button
        complete.addActionListener(e -> {

            String input = JOptionPane.showInputDialog(
                    dialog,
                    "Appointment ID to complete:"
            );

            if (input == null) {
                return;
            }

            try {

                int id = Integer.parseInt(input.trim());

                Appointment a = dao.getAppointmentById(id);

                if (a == null ||
                        a.getDoctorId() != doctorId ||
                        !date.equals(a.getAppointmentDate())) {

                    JOptionPane.showMessageDialog(
                            dialog,
                            "Appointment not found in this doctor/date queue."
                    );

                    return;
                }

                if (!"IN_PROGRESS".equals(a.getStatus())) {

                    JOptionPane.showMessageDialog(
                            dialog,
                            "Only IN_PROGRESS appointments can be completed."
                    );

                    return;
                }

                boolean completed =
                        manager.completeAppointment(id);

                JOptionPane.showMessageDialog(
                        dialog,
                        completed
                                ? "Appointment completed."
                                : "Appointment could not be completed."
                );

                refresh(dialog);

            } catch (Exception ex) {
                error(dialog, ex);
            }
        });


        dialog.setContentPane(this);

        dialog.setSize(900, 550);

        dialog.setLocationRelativeTo(parent);

        refresh(dialog);

        dialog.setVisible(true);
    }


    // Refresh both Currently Serving and Waiting Queue
    private void refresh(Component parent) {

        try {

            model.setRowCount(0);


            // Fetch all appointments
            List<Appointment> allAppointments =
                    dao.getAllAppointments();


            // Find currently serving patient
            Appointment currentServing = null;

            for (Appointment a : allAppointments) {

                if (a.getDoctorId() == doctorId &&
                        date.equals(a.getAppointmentDate()) &&
                        "IN_PROGRESS".equals(a.getStatus())) {

                    currentServing = a;
                    break;
                }
            }


            // Update Currently Serving Section
            if (currentServing == null) {

                currentServingLabel.setText(
                        "No patient currently being served."
                );

            } else {

                currentServingLabel.setText(
                        "<html>" +
                                "<b>Token Number:</b> " +
                                currentServing.getTokenNumber() +

                                " &nbsp;&nbsp; <b>Patient:</b> " +
                                currentServing.getPatientName() +

                                "<br><b>Appointment ID:</b> " +
                                currentServing.getAppointmentId() +

                                " &nbsp;&nbsp; <b>Status:</b> IN_PROGRESS" +
                                "</html>"
                );
            }


            // Populate Waiting Queue Table
            List<Appointment> waiting =
                    manager.getWaitingQueue(doctorId, date);

            for (Appointment a : waiting) {

                model.addRow(new Object[]{
                        a.getTokenNumber(),
                        a.getAppointmentId(),
                        a.getPatientName(),
                        a.getDoctorName(),
                        a.getAppointmentType(),
                        a.getPriority(),
                        a.getStatus()
                });
            }

            if (waiting.isEmpty()) {
                // Empty table is intentional; serving section remains visible.
            }

            revalidate();
            repaint();

        } catch (Exception ex) {
            error(parent, ex);
        }
    }


    // Error handling
    private void error(Component parent, Exception ex) {

        JOptionPane.showMessageDialog(
                parent,
                ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }
}
