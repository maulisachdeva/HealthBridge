
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
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();

        // Main screen container
        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND);

        // Show dashboard first
        showDashboard();

        add(sidebar, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
    }

    // Sidebar
    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setBackground(DARK_BLUE);
        sidebar.setPreferredSize(new Dimension(230, 700));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("HealthBridge");

        title.setForeground(WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setBorder(
                BorderFactory.createEmptyBorder(35, 25, 35, 10)
        );

        sidebar.add(title);

        JButton dashboardButton = createNavButton("Dashboard");
        JButton patientButton = createNavButton("Patient Management");
        JButton doctorButton = createNavButton("Doctor Management");
        JButton appointmentButton = createNavButton("Appointments");
        JButton recordsButton = createNavButton("Medical Records");
        JButton billingButton = createNavButton("Billing & Reports");

        // Button actions
        dashboardButton.addActionListener(e -> showDashboard());

        patientButton.addActionListener(
                e -> showPatientManagement()
        );

        sidebar.add(dashboardButton);
        sidebar.add(patientButton);
        sidebar.add(doctorButton);
        sidebar.add(appointmentButton);
        sidebar.add(recordsButton);
        sidebar.add(billingButton);

        return sidebar;
    }

    // Navigation button
    private JButton createNavButton(String text) {

        JButton button = new JButton(text);

        button.setForeground(WHITE);
        button.setBackground(DARK_BLUE);
        button.setFont(new Font("Arial", Font.PLAIN, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);

        button.setBorder(
                BorderFactory.createEmptyBorder(15, 25, 15, 10)
        );

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 55)
        );

        return button;
    }

    // Show dashboard
    private void showDashboard() {

        mainContent.removeAll();

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BACKGROUND);

        content.setBorder(
                BorderFactory.createEmptyBorder(30, 35, 30, 35)
        );

        JLabel heading = new JLabel("Welcome to HealthBridge");

        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Smart Clinic Management and Queue Automation System"
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel();

        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BACKGROUND);

        header.add(heading);
        header.add(Box.createVerticalStrut(10));
        header.add(subtitle);

        content.add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 20));

        cards.setBackground(BACKGROUND);
        cards.setBorder(
                BorderFactory.createEmptyBorder(40, 0, 0, 0)
        );

        cards.add(createDashboardCard(
                "Patient Management",
                "Manage patient registrations and records"
        ));

        cards.add(createDashboardCard(
                "Doctor Management",
                "Manage doctor profiles and specializations"
        ));

        content.add(cards, BorderLayout.CENTER);

        mainContent.add(content, BorderLayout.CENTER);

        mainContent.revalidate();
        mainContent.repaint();
    }

    // Dashboard card
    private JPanel createDashboardCard(
            String title,
            String description) {

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

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        titleLabel.setForeground(DARK_BLUE);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        descriptionLabel.setForeground(Color.GRAY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(descriptionLabel);

        return card;
    }

    // Show patient management screen
    private void showPatientManagement() {

        mainContent.removeAll();

        mainContent.add(
                new PatientPanel(),
                BorderLayout.CENTER
        );

        mainContent.revalidate();
        mainContent.repaint();
    }
}
