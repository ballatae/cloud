package com.example.restapi.dao;
import jakarta.json.JsonValue;
public class Patient {
    private long patientId;
    private String name;
    private String surname;

    // Constructors
    public Patient() {
    }

    public Patient(long patientId, String name, String surname) {
        this.patientId = patientId;
        this.name = name;
        this.surname = surname;
    }

    // Getter and Setter methods
    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
