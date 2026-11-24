
package com.healthbridge.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final Color DARK_BLUE = new Color(25, 45, 85);
    private final Color BACKGROUND = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;

    private JPanel mainContent;

    public MainFrame() {

        setTitle("HealthBridge - Smart Clinic Management System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setSize(1200, 750);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel sidebar = createSidebar();

        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND);

        showDashboard();

        add(sidebar, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
    }

    // Create sidebar
    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setBackground(DARK_BLUE);
        sidebar.setPreferredSize(new Dimension(230, 700));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("HealthBridge");

        title.setForeground(WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(
                BorderFactory.createEmptyBorder(30, 20, 30, 10)
        );

        sidebar.add(title);

        JButton dashboardButton = createNavButton("Dashboard");
        JButton patientButton = createNavButton("Patient Management");
        JButton doctorButton = createNavButton("Doctor Management");
        JButton appointmentButton = createNavButton("Appointments");
        JButton recordsButton = createNavButton("Medical Records");
        JButton prescriptionButton = createNavButton("Prescriptions");
        JButton billingButton = createNavButton("Billing & Reports");

        // Navigation actions
        dashboardButton.addActionListener(e -> showDashboard());
        patientButton.addActionListener(e -> showPatientManagement());
        doctorButton.addActionListener(e -> showDoctorManagement());

        recordsButton.addActionListener(e -> showMedicalRecords());
        prescriptionButton.addActionListener(e -> showPrescriptions());

        // Add buttons to sidebar
        sidebar.add(dashboardButton);
        sidebar.add(patientButton);
        sidebar.add(doctorButton);
        sidebar.add(appointmentButton);
        sidebar.add(recordsButton);
        sidebar.add(prescriptionButton);
        sidebar.add(billingButton);

        // Make sidebar scrollable
        JScrollPane sidebarScroll = new JScrollPane(sidebar);

        sidebarScroll.setPreferredSize(new Dimension(230, 700));

        sidebarScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        sidebarScroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        sidebarScroll.setBorder(null);

        return createSidebarWrapper(sidebarScroll);
    }

    private JPanel createSidebarWrapper(JScrollPane scrollPane) {

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(DARK_BLUE);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(230, 700));

        return wrapper;
    }

    // Create navigation button
    private JButton createNavButton(String text) {

        JButton button = new JButton(text);

        button.setForeground(WHITE);
        button.setBackground(DARK_BLUE);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        button.setPreferredSize(new Dimension(220, 55));

        button.setBorder(
                BorderFactory.createEmptyBorder(12, 20, 12, 8)
        );

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // Show dashboard
    private void showDashboard() {

        mainContent.removeAll();

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(BACKGROUND);

        content.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        JLabel heading = new JLabel("Welcome to HealthBridge");

        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "<html>Smart Clinic Management and Queue Automation System</html>"
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel();

        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BACKGROUND);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(heading);
        header.add(Box.createVerticalStrut(10));
        header.add(subtitle);

        content.add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(0, 2, 20, 20));

        cards.setBackground(BACKGROUND);

        cards.setBorder(
                BorderFactory.createEmptyBorder(35, 0, 0, 0)
        );

        cards.add(createDashboardCard(
                "Patient Management",
                "Manage patient registrations and records"
        ));

        cards.add(createDashboardCard(
                "Doctor Management",
                "Manage doctor profiles and specializations"
        ));

        JPanel cardContainer = new JPanel(new BorderLayout());

        cardContainer.setBackground(BACKGROUND);
        cardContainer.add(cards, BorderLayout.NORTH);

        content.add(cardContainer, BorderLayout.CENTER);

        mainContent.add(content, BorderLayout.CENTER);

        mainContent.revalidate();
        mainContent.repaint();
    }

    // Create dashboard card
    private JPanel createDashboardCard(String title, String description) {

        JPanel card = new JPanel();

        card.setBackground(WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 240)
                        ),
                        BorderFactory.createEmptyBorder(
                                25, 20, 25, 20
                        )
                )
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(DARK_BLUE);

        JLabel descriptionLabel = new JLabel(
                "<html><div style='width:180px;'>"
                        + description
                        + "</div></html>"
        );

        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        descriptionLabel.setForeground(Color.GRAY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(descriptionLabel);

        return card;
    }

    // Show patient management
    private void showPatientManagement() {

        mainContent.removeAll();

        mainContent.add(
                new PatientPanel(),
                BorderLayout.CENTER
        );

        mainContent.revalidate();
        mainContent.repaint();
    }

    // Show doctor management
    private void showDoctorManagement() {

        mainContent.removeAll();

        mainContent.add(
                new DoctorPanel(),
                BorderLayout.CENTER
        );

        mainContent.revalidate();
        mainContent.repaint();
    }

    // Show medical records
    private void showMedicalRecords() {

        mainContent.removeAll();

        mainContent.add(
                new MedicalRecordPanel(),
                BorderLayout.CENTER
        );

        mainContent.revalidate();
        mainContent.repaint();
    }

    // Show prescriptions
    private void showPrescriptions() {

        mainContent.removeAll();

        mainContent.add(
                new PrescriptionPanel(),
                BorderLayout.CENTER
        );

        mainContent.revalidate();
        mainContent.repaint();
    }
}