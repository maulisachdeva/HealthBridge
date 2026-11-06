
package com.healthbridge;

import com.healthbridge.dao.DoctorDAO;
import com.healthbridge.model.Doctor;

public class Main {

    public static void main(String[] args) {

        DoctorDAO doctorDAO = new DoctorDAO();

        // Step 1: Find the test doctor
        int doctorId = 1;

        Doctor doctor = doctorDAO.getDoctorById(doctorId);

        if (doctor != null) {

            System.out.println("Doctor found: "
                    + doctor.getName());

            // Step 2: Delete doctor
            boolean result = doctorDAO.deleteDoctor(doctorId);

            if (result) {
                System.out.println(
                        "Doctor deleted successfully!"
                );
            } else {
                System.out.println(
                        "Doctor deletion failed!"
                );
            }

            // Step 3: Verify deletion
            Doctor checkDoctor =
                    doctorDAO.getDoctorById(doctorId);

            if (checkDoctor == null) {
                System.out.println(
                        "Verification successful: Doctor no longer exists."
                );
            } else {
                System.out.println(
                        "Verification failed: Doctor still exists."
                );
            }

        } else {
            System.out.println("Doctor not found!");
        }
    }
}
