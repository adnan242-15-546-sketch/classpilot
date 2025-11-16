package com.university.model;

// Inheritance
public class Teacher extends User {

    private String designation; // e.g., "Professor", "Lecturer"

    public Teacher(String userId, String password, String fullName, String designation) {
        super(userId, password, fullName);
        this.designation = designation;
    }

    public String getDesignation() {
        return designation;
    }

    // Polymorphism
    @Override
    public String getRole() {
        return "Teacher";
    }
}