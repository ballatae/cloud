package com.example.restapi.dao;

public class User {
private int id;
private String name;
private String surname;
private String username;
private String password;
private String role;

//Empty constructor
public User() {
    // Default constructor with no parameters
}

// Constructor with all values
public User(int id,String name,String surname, String username, String password, String role) {
    this.id = id;
    this.name = name;
    this.surname = surname;
    this.username = username;
    this.password = password;
    this.role = role;
}

// Getter and setter methods remain the same as in the previous response

// Getter methods
public int getId() {
    return id;
}
public String getName() {
    return name;
}
public String getSurname() {
    return surname;
}

public String getUsername() {
    return username;
}

public String getPassword() {
    return password;
}

public String getRole() {
    return role;
}

// Setter methods
public void setId(int id) {
    this.id = id;
}
public void setName(String name) {
    this.name = name;
}
public void setSurname(String surname) {
    this.surname = surname;
}

public void setUsername(String username) {
    this.username = username;
}

public void setPassword(String password) {
    this.password = password;
}

public void setRole(String role) {
    this.role = role;
}
}

