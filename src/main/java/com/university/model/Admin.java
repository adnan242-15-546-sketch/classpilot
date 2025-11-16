package com.university.model;

// Inheritance
public class Admin extends User {

    public Admin(String userId, String password, String fullName) {
        super(userId, password, fullName);
    }

    // Polymorphism
    @Override
    public String getRole() {
        return "Admin";
    }
}