package com.healthbridge.ui;

import com.healthbridge.dao.BillDAO;
import com.healthbridge.model.Bill;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BillingPanel extends JPanel {

    private final Color DARK_BLUE = new Color(25, 45, 85);
    private final Color BACKGROUND = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;

    private BillDAO billDAO;

    private JLabel totalBillsLabel;
    private JLabel revenueLabel;
    private JLabel pendingLabel;

    private DefaultTableModel tableModel;

    public BillingPanel() {

        billDAO = new BillDAO();

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        refreshData();
    }

    // Header
    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(BACKGROUND);

        JLabel title = new JLabel("Billing & Reports");

        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Manage bills, payments and revenue"
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(BACKGROUND);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(subtitle);

        JButton generateButton = new JButton("Generate New Bill");

        generateButton.setBackground(DARK_BLUE);
        generateButton.setForeground(WHITE);
        generateButton.setFocusPainted(false);

        generateButton.addActionListener(e -> openGenerateBill());

        header.add(textPanel, BorderLayout.WEST);
        header.add(generateButton, BorderLayout.EAST);

        return header;
    }

    // Main content
    private JPanel createMainContent() {

        JPanel content = new JPanel(new BorderLayout(15, 20));

        content.setBackground(BACKGROUND);
        content.setBorder(
                BorderFactory.createEmptyBorder(30, 0, 0, 0)
        );

        // Statistics cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 15));

        cards.setBackground(BACKGROUND);

        totalBillsLabel = new JLabel("0");
        revenueLabel = new JLabel("₹0.00");
        pendingLabel = new JLabel("₹0.00");

        cards.add(createCard("Total Bills", totalBillsLabel));
        cards.add(createCard("Total Revenue", revenueLabel));
        cards.add(createCard("Pending Amount", pendingLabel));

        // Table
        String[] columns = {
                "Bill ID",
                "Patient ID",
                "Appointment ID",
                "Total Amount",
                "Payment Status",
                "Bill Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);

        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createTitledBorder("Recent Bills")
        );

        JPanel tablePanel = new JPanel(new BorderLayout());

        tablePanel.setBackground(WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");

        refreshButton.addActionListener(e -> refreshData());

        tablePanel.add(refreshButton, BorderLayout.SOUTH);

        content.add(cards, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);

        return content;
    }

    // Dashboard card
    private JPanel createCard(String title, JLabel value) {

        JPanel card = new JPanel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 240)
                        ),
                        BorderFactory.createEmptyBorder(
                                20, 20, 20, 20
                        )
                )
        );

        JLabel heading = new JLabel(title);

        heading.setFont(new Font("Arial", Font.PLAIN, 14));
        heading.setForeground(Color.GRAY);

        value.setFont(new Font("Arial", Font.BOLD, 25));
        value.setForeground(DARK_BLUE);

        card.add(heading);
        card.add(Box.createVerticalStrut(15));
        card.add(value);

        return card;
    }

    // Load database data
    public void refreshData() {

        totalBillsLabel.setText(
                String.valueOf(billDAO.getTotalBillCount())
        );

        revenueLabel.setText(
                String.format("₹%.2f", billDAO.getTotalRevenue())
        );

        pendingLabel.setText(
                String.format("₹%.2f", billDAO.getPendingAmount())
        );

        tableModel.setRowCount(0);

        List<Bill> bills = billDAO.getAllBills();

        for (Bill bill : bills) {

            tableModel.addRow(new Object[]{

                    bill.getBillId(),
                    bill.getPatientId(),
                    bill.getAppointmentId(),
                    String.format("₹%.2f", bill.getTotalAmount()),
                    bill.getPaymentStatus(),
                    bill.getBillDate()

            });
        }
    }

    // Open Generate Bill
    private void openGenerateBill() {

        // JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

        JPanel container = (JPanel) getParent();

        if (container == null) {
            return;
        }

        container.removeAll();

        container.add(
                new GenerateBillPanel(() -> {
                    container.removeAll();
                    container.add(this, BorderLayout.CENTER);
                    refreshData();
                    container.revalidate();
                    container.repaint();
                }),
                BorderLayout.CENTER
        );

        container.revalidate();
        container.repaint();
    }
}