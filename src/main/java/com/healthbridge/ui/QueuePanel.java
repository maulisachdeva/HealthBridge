package com.healthbridge.ui;
import com.healthbridge.dao.AppointmentDAO;
import com.healthbridge.dao.DoctorDAO;
import com.healthbridge.model.Doctor;
import java.util.List;
import com.healthbridge.model.Appointment;
import com.healthbridge.service.QueueManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
public class QueuePanel extends JPanel {
    private final QueueManager manager=new QueueManager();
    private final AppointmentDAO dao=new AppointmentDAO();
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"Token","Appointment ID","Patient","Doctor","Type","Priority","Status"},0){public boolean isCellEditable(int r,int c){return false;}};
    private int doctorId;
    private LocalDate date;
    public QueuePanel(){setLayout(new BorderLayout(10,10));}
    public void open(Component parent){
        List<Doctor> doctors=new DoctorDAO().getAllDoctors();
        if(doctors.isEmpty()){JOptionPane.showMessageDialog(parent,"No doctors available.");return;}
        JComboBox<String> doctor=new JComboBox<>();
        for(Doctor d:doctors)doctor.addItem(d.getDoctorId()+" - "+d.getName());
        JTextField day=new JTextField(LocalDate.now().toString());
        JPanel filter=new JPanel(new GridLayout(2,2,10,10));filter.add(new JLabel("Doctor:"));filter.add(doctor);filter.add(new JLabel("Date (YYYY-MM-DD):"));filter.add(day);
        if(JOptionPane.showConfirmDialog(parent,filter,"Select Queue",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{doctorId=doctors.get(doctor.getSelectedIndex()).getDoctorId();date=LocalDate.parse(day.getText().trim());if(doctorId<=0)throw new IllegalArgumentException("Invalid doctor ID");}
        catch(Exception ex){error(parent,ex);return;}
        removeAll();
        JLabel title=new JLabel("Queue for "+doctors.get(doctor.getSelectedIndex()).getName()+" on "+date);title.setFont(new Font("Arial",Font.BOLD,18));title.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));add(title,BorderLayout.NORTH);
        JTable table=new JTable(model);table.setRowHeight(27);add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel controls=new JPanel(new FlowLayout());JButton refresh=new JButton("Refresh Queue"),call=new JButton("Call Next Patient"),complete=new JButton("Complete Appointment");
        controls.add(refresh);controls.add(call);controls.add(complete);add(controls,BorderLayout.SOUTH);
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(parent),"Smart Queue",Dialog.ModalityType.APPLICATION_MODAL);
        refresh.addActionListener(e->refresh(dialog));
        call.addActionListener(e->{try{Appointment next=manager.callNextPatient(doctorId,date);JOptionPane.showMessageDialog(dialog,next==null?"No waiting patients.":"Calling Token "+next.getTokenNumber()+"\nPatient: "+next.getPatientName()+"\nDoctor: "+next.getDoctorName()+"\nAppointment ID: "+next.getAppointmentId());refresh(dialog);}catch(Exception ex){error(dialog,ex);}});
        complete.addActionListener(e->{String input=JOptionPane.showInputDialog(dialog,"Appointment ID to complete:");if(input==null)return;try{
            int id=Integer.parseInt(input.trim());Appointment a=dao.getAppointmentById(id);
            if(a==null||a.getDoctorId()!=doctorId||!date.equals(a.getAppointmentDate())){JOptionPane.showMessageDialog(dialog,"Appointment not found in this doctor/date queue.");return;}
            if(!"IN_PROGRESS".equals(a.getStatus())){JOptionPane.showMessageDialog(dialog,"Only IN_PROGRESS appointments can be completed.");return;}
            JOptionPane.showMessageDialog(dialog,manager.completeAppointment(id)?"Appointment completed.":"Appointment could not be completed.");refresh(dialog);
        }catch(Exception ex){error(dialog,ex);}});
        dialog.setContentPane(this);dialog.setSize(850,480);dialog.setLocationRelativeTo(parent);refresh(dialog);dialog.setVisible(true);
    }
    private void refresh(Component parent){try{model.setRowCount(0);for(Appointment a:manager.getWaitingQueue(doctorId,date))model.addRow(new Object[]{a.getTokenNumber(),a.getAppointmentId(),a.getPatientName(),a.getDoctorName(),a.getAppointmentType(),a.getPriority(),a.getStatus()});}catch(Exception ex){error(parent,ex);}}
    private void error(Component parent,Exception ex){JOptionPane.showMessageDialog(parent,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);ex.printStackTrace();}
}
