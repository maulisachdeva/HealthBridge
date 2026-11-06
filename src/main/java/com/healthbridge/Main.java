
package com.healthbridge;

import com.healthbridge.dao.PatientDAO;
import com.healthbridge.model.Patient;

public class Main {

    public static void main(String[] args) {

        PatientDAO patientDAO = new PatientDAO();

        // Step 1: Create a separate test patient
        Patient testPatient = new Patient(
                0,
                "Delete Test Patient",
                30,
                "Other",
                "9999900002",
                "delete.test@example.com",
                "Temporary Test Address"
        );

        // Step 2: Register the test patient
        boolean added = patientDAO.addPatient(testPatient);

        if (added) {

            System.out.println("Test patient registered!");

            // Step 3: Find the newly registered patient
            Patient savedPatient = null;

            for (Patient patient : patientDAO.getAllPatients()) {

                if (patient.getPhone().equals("9999900002")) {
                    savedPatient = patient;
                    break;
                }
            }

            // Step 4: Delete the test patient
            if (savedPatient != null) {

                int patientId = savedPatient.getPatientId();

                System.out.println("Patient ID: " + patientId);

                boolean deleted =
                        patientDAO.deletePatient(patientId);

                if (deleted) {
                    System.out.println(
                            "Test patient deleted successfully!"
                    );
                } else {
                    System.out.println(
                            "Patient deletion failed!"
                    );
                }

                // Step 5: Verify deletion
                Patient checkPatient =
                        patientDAO.getPatientById(patientId);

                if (checkPatient == null) {
                    System.out.println(
                            "Verification successful: Patient no longer exists."
                    );
                } else {
                    System.out.println(
                            "Verification failed: Patient still exists."
                    );
                }

            } else {
                System.out.println("Could not find test patient!");
            }

        } else {
            System.out.println(
                    "Test patient registration failed!"
            );
        }
    }
}
