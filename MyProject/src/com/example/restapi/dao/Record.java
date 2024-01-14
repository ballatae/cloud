// src/main/java/com/example/restapi/model/DailyRecord.java
package com.example.restapi.dao;

import java.sql.Date;

import jakarta.json.JsonValue;

public class Record {

    private Long id;
    private int glucoseLevel;
    private int carbIntake;
    private String medicationDose;
    private String entryDate;
    private int patientId;
    

    public Record() {
        // Default constructor
    }

    public Record(Long id, int glucoseLevel, int carbIntake, String medicationDose, String entryDate, int patientId) {
        this.id = id;
        this.glucoseLevel = glucoseLevel;
        this.carbIntake = carbIntake;
        this.medicationDose = medicationDose;
        this.entryDate = entryDate;
        this.patientId = patientId;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getGlucoseLevel() {
        return glucoseLevel;
    }

    public void setGlucoseLevel(int glucoseLevel) {
        this.glucoseLevel = glucoseLevel;
    }

    public int getCarbIntake() {
        return carbIntake;
    }

    public void setCarbIntake(int carbIntake) {
        this.carbIntake = carbIntake;
    }

    public String getMedicationDose() {
        return medicationDose;
    }

    public void setMedicationDose(String medicationDose) {
        this.medicationDose = medicationDose;
    }
    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    // ... existing methods ...

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", glucoseLevel=" + glucoseLevel +
                ", carbIntake=" + carbIntake +
                ", medicationDose='" + medicationDose + '\'' +
                ", entryDate=" + entryDate +
                ", patientid=" + patientId+
                '}';
    }

	

	
}
