package com.university.model;

import java.io.*;
import java.util.ArrayList;

public class DatabaseService {

    // Lists
    private ArrayList<User> userList;
    private ArrayList<Course> courseList;
    private ArrayList<RoutineSlot> routineList;
    private ArrayList<Notice> noticeList;

    // File Names
    private static final String USER_FILE = "users.dat";
    private static final String COURSE_FILE = "courses.dat";
    private static final String ROUTINE_FILE = "routines.dat";
    private static final String NOTICE_FILE = "notices.dat";

    public DatabaseService() {
        // Load all data
        this.userList = loadData(USER_FILE);
        this.courseList = loadData(COURSE_FILE);
        this.routineList = loadData(ROUTINE_FILE);
        this.noticeList = loadData(NOTICE_FILE);

        // Initialize if null
        if (userList == null) userList = new ArrayList<>();
        if (courseList == null) courseList = new ArrayList<>();
        if (routineList == null) routineList = new ArrayList<>();
        if (noticeList == null) noticeList = new ArrayList<>();
    }

    // --- Getters ---
    public ArrayList<User> getUserList() { return userList; }
    public ArrayList<Course> getCourseList() { return courseList; }
    public ArrayList<RoutineSlot> getRoutineList() { return routineList; }
    public ArrayList<Notice> getNoticeList() { return noticeList; }

    // --- Add Methods ---
    public void addUser(User user) {
        userList.add(user);
        saveData(userList, USER_FILE);
    }

    public void addCourse(Course course) {
        courseList.add(course);
        saveData(courseList, COURSE_FILE);
    }

    public void addRoutineSlot(RoutineSlot slot) {
        routineList.add(slot);
        saveData(routineList, ROUTINE_FILE);
    }

    // --- Add new notice ---
    public void addNotice(Notice notice) {
        noticeList.add(notice);
        saveData(noticeList, NOTICE_FILE);
    }

    // --- Delete / Update Helpers ---
    public void deleteUser(User user) {
        userList.remove(user);
        saveData(userList, USER_FILE);
    }

    public void updateUser(User oldUser, User newUser) {
        int index = userList.indexOf(oldUser);
        if (index != -1) {
            userList.set(index, newUser);
            saveData(userList, USER_FILE);
        }
    }

    public void deleteCourse(Course course) {
        courseList.remove(course);
        saveData(courseList, COURSE_FILE);
    }

    public void deleteRoutine(RoutineSlot slot) {
        routineList.remove(slot);
        saveData(routineList, ROUTINE_FILE);
    }

    // --- Notice Delete Method ---
    public void deleteNotice(Notice notice) {
        if (noticeList.remove(notice)) {
            saveData(noticeList, NOTICE_FILE);
            System.out.println("Notice deleted successfully.");
        }
    }

    // --- Generic Save/Load Logic ---
    private <T> void saveData(ArrayList<T> list, String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(list);
            // System.out.println("Saved: " + fileName); // Optional log
        } catch (IOException e) {
            System.err.println("Error saving " + fileName + ": " + e.getMessage());
        }
    }

    private <T> ArrayList<T> loadData(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (ArrayList<T>) ois.readObject();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}