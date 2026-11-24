package com.healthbridge.ui;
import com.healthbridge.dao.AppointmentDAO;
import com.healthbridge.dao.DoctorDAO;
import com.healthbridge.dao.PatientDAO;
import com.healthbridge.model.Appointment;
import com.healthbridge.model.Doctor;
import com.healthbridge.model.Patient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeParseException;
public class AppointmentPanel extends JPanel {
    private final AppointmentDAO dao=new AppointmentDAO();
    public AppointmentPanel(){
        setLayout(new BorderLayout(12,12));setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JLabel title=new JLabel("Appointment Management");title.setFont(new Font("Arial",Font.BOLD,26));
        GridLayout buttonLayout=new GridLayout(0,4,10,10);
        JPanel buttons=new JPanel(buttonLayout);
        JButton book=new JButton("Book Appointment"),view=new JButton("View Appointments"),queue=new JButton("Smart Queue"),cancel=new JButton("Cancel Appointment");
        buttons.add(book);buttons.add(view);buttons.add(queue);buttons.add(cancel);
        JPanel top=new JPanel(new BorderLayout());top.add(title,BorderLayout.NORTH);top.add(buttons,BorderLayout.CENTER);add(top,BorderLayout.NORTH);
        addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override
            public void componentResized(java.awt.event.ComponentEvent e){
                int available=getWidth()-getInsets().left-getInsets().right;
                int buttonWidth=0;
                for(Component button:buttons.getComponents()){
                    buttonWidth=Math.max(buttonWidth,button.getPreferredSize().width);
                }
                int columns=available>=4*buttonWidth+30?4:available>=2*buttonWidth+10?2:1;
                if(buttonLayout.getColumns()!=columns){
                    buttonLayout.setColumns(columns);
                    buttons.revalidate();
                    top.revalidate();
                    AppointmentPanel.this.revalidate();
                    AppointmentPanel.this.repaint();
                }
            }
        });
        book.addActionListener(e->book());view.addActionListener(e->view());queue.addActionListener(e->new QueuePanel().open(this));cancel.addActionListener(e->cancel());
    }
    private void book(){
        JComboBox<Choice> patients=new JComboBox<>(),doctors=new JComboBox<>();
        for(Patient p:new PatientDAO().getAllPatients())patients.addItem(new Choice(p.getPatientId(),p.getName()));
        for(Doctor d:new DoctorDAO().getAllDoctors())doctors.addItem(new Choice(d.getDoctorId(),d.getName()+" - "+d.getSpecialization()));
        if(patients.getItemCount()==0||doctors.getItemCount()==0){message("Register at least one patient and doctor first.");return;}
        JTextField date=new JTextField(LocalDate.now().toString()),time=new JTextField(""),reason=new JTextField();
        JComboBox<String> type=new JComboBox<>(new String[]{"SCHEDULED","WALK_IN"}),priority=new JComboBox<>(new String[]{"NORMAL","EMERGENCY"});
        JLabel timeLabel=new JLabel("Time (HH:MM):");
        type.addActionListener(e->{boolean scheduled="SCHEDULED".equals(type.getSelectedItem());time.setEnabled(scheduled);timeLabel.setEnabled(scheduled);});
        JPanel form=new JPanel(new GridLayout(0,2,10,10));
        form.add(new JLabel("Patient:"));form.add(patients);form.add(new JLabel("Doctor:"));form.add(doctors);
        form.add(new JLabel("Date (YYYY-MM-DD):"));form.add(date);form.add(new JLabel("Appointment type:"));form.add(type);
        form.add(timeLabel);form.add(time);form.add(new JLabel("Priority:"));form.add(priority);form.add(new JLabel("Reason:"));form.add(reason);
        if(JOptionPane.showConfirmDialog(this,form,"Book Appointment",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
        try{
            Appointment a=new Appointment();a.setPatientId(((Choice)patients.getSelectedItem()).id);a.setDoctorId(((Choice)doctors.getSelectedItem()).id);
            a.setAppointmentDate(LocalDate.parse(date.getText().trim(),DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)));a.setAppointmentType((String)type.getSelectedItem());
            a.setAppointmentTime("SCHEDULED".equals(a.getAppointmentType())?LocalTime.parse(time.getText().trim(),DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)):null);
            a.setPriority((String)priority.getSelectedItem());a.setReason(reason.getText().trim());
            if(dao.addAppointment(a))message("Appointment booked successfully!\nAppointment ID: "+a.getAppointmentId()+"\nToken: "+a.getTokenNumber());
        }catch(DateTimeParseException ex){message("Enter a valid date (YYYY-MM-DD) and scheduled time (HH:MM).");}
        catch(Exception ex){error(ex);}
    }
    private void view(){
        try{
            DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Patient","Doctor","Date","Time","Token","Type","Priority","Status"},0){public boolean isCellEditable(int r,int c){return false;}};
            for(Appointment a:dao.getAllAppointments())model.addRow(new Object[]{a.getAppointmentId(),a.getPatientName(),a.getDoctorName(),a.getAppointmentDate(),a.getAppointmentTime(),a.getTokenNumber(),a.getAppointmentType(),a.getPriority(),a.getStatus()});
            JTable table=new JTable(model);table.setRowHeight(26);JScrollPane scroll=new JScrollPane(table);scroll.setPreferredSize(new Dimension(900,400));
            JOptionPane.showMessageDialog(this,scroll,"Appointments",JOptionPane.PLAIN_MESSAGE);
        }catch(Exception ex){error(ex);}
    }
    private void cancel(){
        String input=JOptionPane.showInputDialog(this,"Appointment ID to cancel:");if(input==null)return;
        try{
            int id=Integer.parseInt(input.trim());Appointment a=dao.getAppointmentById(id);
            if(a==null){message("Appointment not found.");return;}
            if(!"WAITING".equals(a.getStatus())){message("Only waiting appointments can be cancelled.");return;}
            if(JOptionPane.showConfirmDialog(this,"Cancel appointment "+id+"?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            message(dao.cancelAppointment(id)?"Appointment cancelled successfully.":"Appointment could not be cancelled.");
        }catch(Exception ex){error(ex);}
    }
    private void message(String s){JOptionPane.showMessageDialog(this,s);}
    private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);ex.printStackTrace();}
    private static class Choice {
        final int id;final String name;
        Choice(int id,String name){this.id=id;this.name=name;}
        public String toString(){return id+" - "+name;}
    }
}
