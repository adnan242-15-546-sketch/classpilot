package com.university.model;
import java.io.Serializable;

public abstract class User implements Serializable {


    private String userId;
    private String password;
    private String fullName;


    public User(String userId, String password, String fullName) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
    }


    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }


    public boolean validatePassword(String inputPassword) {

        return this.password.equals(inputPassword);
    }


    public abstract String getRole();
}