package com.university.model;

import java.io.Serializable;

public class RoutineSlot implements Serializable {
    private String day;
    private String time;
    private String roomNo;
    private Course course;
    private String teacherName;
    private String section;

    // Constructor to initialize the routine slot
    public RoutineSlot(String day, String time, String roomNo, Course course, String teacherName, String section) {
        this.day = day;
        this.time = time;
        this.roomNo = roomNo;
        this.course = course;
        this.teacherName = teacherName;
        this.section = section;
    }

    // --- Getters ---
    public String getDay() { return day; }
    public String getTime() { return time; }
    public String getRoomNo() { return roomNo; }
    public String getSection() { return section; }
    public String getTeacherName() { return teacherName; }

    // Getter for the Course object
    public Course getCourse() { return course; }


    public String getCourseInfo() {
        return course.getCourseCode() + "\n" + course.getCourseTitle();
    }
}