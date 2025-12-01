package com.university.model;

import java.io.Serializable;

public class Course implements Serializable {
    private String courseCode;
    private String courseTitle;
    private double credits;


    public Course(String courseCode, String courseTitle, double credits) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
    }


    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public double getCredits() {
        return credits;
    }


    @Override
    public String toString() {
        return courseCode + " - " + courseTitle;
    }
}