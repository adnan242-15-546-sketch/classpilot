package com.university.model;

import java.io.*;
import java.util.ArrayList;

public class DatabaseService {


    private ArrayList<User> userList;
    private ArrayList<Course> courseList;
    private ArrayList<RoutineSlot> routineList;


    private static final String USER_FILE = "users.dat";
    private static final String COURSE_FILE = "courses.dat";
    private static final String ROUTINE_FILE = "routines.dat";

    public DatabaseService() {

        this.userList = loadData(USER_FILE);
        this.courseList = loadData(COURSE_FILE);
        this.routineList = loadData(ROUTINE_FILE);


        if (userList == null) userList = new ArrayList<>();
        if (courseList == null) courseList = new ArrayList<>();
        if (routineList == null) routineList = new ArrayList<>();
    }

    // --- Getters ---
    public ArrayList<User> getUserList() { return userList; }
    public ArrayList<Course> getCourseList() { return courseList; }
    public ArrayList<RoutineSlot> getRoutineList() { return routineList; }

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

    // --- Generic Save/Load Helpers ---


    private <T> void saveData(ArrayList<T> list, String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(list);
            System.out.println("Saved data to: " + fileName);
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
            e.printStackTrace();
            return null;
        }
    }
}