package com.university.model;

import java.io.*;
import java.util.ArrayList;

public class DatabaseService {


    private ArrayList<User> userList;


    private static final String FILE_NAME = "users.dat"; // .dat বা .ser দিতে পারো

    // Constructor
    public DatabaseService() {
        this.userList = new ArrayList<>();
        loadData();
    }


    public ArrayList<User> getUserList() {
        return userList;
    }


    public void addUser(User user) {
        this.userList.add(user);
        saveData();
    }


    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(userList); // পুরো ইউজার লিস্টটা ফাইলে লিখে দিলাম
            System.out.println("Data saved successfully!");
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }


    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            userList = (ArrayList<User>) ois.readObject();
            System.out.println("Data loaded successfully!");
        } catch (FileNotFoundException e) {
            System.out.println("No saved data found. Starting fresh.");

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}