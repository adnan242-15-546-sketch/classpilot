package com.university.model;


public class Student extends User {

    private String batch;


    // Constructor
    public Student(String userId, String password, String fullName, String batch) {

        super(userId, password, fullName);
        this.batch = batch;
    }

    // Getter
    public String getBatch() {
        return batch;
    }


    @Override
    public String getRole() {
        return "Student";
    }
}