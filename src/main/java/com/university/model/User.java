package com.university.model;
import java.io.Serializable;

public abstract class User implements Serializable {

    // Encapsulation: ডেটাগুলো private রাখা
    private String userId;
    private String password;
    private String fullName;

    // Constructor: User অবজেক্ট তৈরির সময় এই ডেটাগুলো লাগবেই
    public User(String userId, String password, String fullName) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
    }

    // Getters: private ডেটাগুলো বাইরে থেকে দেখার জন্য
    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    // --- মূল লজিক ---

    // পাসওয়ার্ড চেক করার মেথড
    public boolean validatePassword(String inputPassword) {
        // সহজ পাসওয়ার্ড চেকিং
        return this.password.equals(inputPassword);
    }

    // Polymorphism: এই মেথডটা Student/Teacher/Admin ক্লাসে থাকতেই হবে
    // কিন্তু একেকজনের জন্য এর কাজ একেকরকম হবে
    public abstract String getRole();
}