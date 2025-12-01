package com.university.controller;

import com.university.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminDashboardController {

    // TAB 1: USER MANAGEMENT

    @FXML private TextField userIdField;
    @FXML private TextField passwordField;
    @FXML private TextField fullNameField;
    @FXML private ChoiceBox<String> roleSelector;
    @FXML private TextField detailsField;
    @FXML private Label statusLabel;
    @FXML private Button logoutButton;

    // User Table
    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, String> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> roleColumn;


    // TAB 2: ROUTINE MANAGEMENT

    // Section A: Add Course
    @FXML private TextField courseCodeField;
    @FXML private TextField courseTitleField;
    @FXML private TextField creditField;
    @FXML private ListView<Course> courseListView; // ListView to display added courses

    // Section B: Add Routine Slot
    @FXML private ChoiceBox<String> daySelector;
    @FXML private TextField timeField;
    @FXML private TextField roomField;
    @FXML private TextField sectionField;
    @FXML private ChoiceBox<Course> courseSelector; // Holds Course objects directly
    @FXML private ChoiceBox<String> teacherSelector; // Populated with teacher names

    // --- Database Service ---
    private DatabaseService databaseService = new DatabaseService();

    @FXML
    public void initialize() {
        // --- TAB 1 INITIALIZATION ---
        roleSelector.getItems().addAll("Student", "Teacher");
        roleSelector.setValue("Student");

        // Setup TableView Columns using Lambda expressions
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        loadUserData();

        // --- TAB 2 INITIALIZATION ---

        // Populate days
        daySelector.getItems().addAll("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        daySelector.setValue("Sunday");

        // Load teachers and courses
        loadTeachersIntoSelector();
        refreshCourseList();
    }


    // TAB 1 ACTIONS (User)

    @FXML
    private void onCreateUserButtonClick() {
        String userId = userIdField.getText();
        String password = passwordField.getText();
        String fullName = fullNameField.getText();
        String role = roleSelector.getValue();
        String details = detailsField.getText();

        // Input Validation
        if (userId.isEmpty() || password.isEmpty() || fullName.isEmpty() || details.isEmpty()) {
            statusLabel.setText("Fill all fields!");
            return;
        }

        // Create new User object based on role
        User newUser;
        if (role.equals("Student")) {
            newUser = new Student(userId, password, fullName, details);
        } else {
            newUser = new Teacher(userId, password, fullName, details);
        }

        // Save to database
        databaseService.addUser(newUser);
        statusLabel.setText("User created!");

        // Update UI components
        loadUserData();
        loadTeachersIntoSelector();
        clearUserFields();
    }


    // TAB 2 ACTIONS (Routine)

    // Action to add a new course
    @FXML
    private void onAddCourseClick() {
        String code = courseCodeField.getText();
        String title = courseTitleField.getText();
        String creditStr = creditField.getText();

        if (code.isEmpty() || title.isEmpty() || creditStr.isEmpty()) {
            showAlert("Error", "Please fill all course fields.");
            return;
        }

        try {
            double credits = Double.parseDouble(creditStr);
            Course newCourse = new Course(code, title, credits);

            // Save course to database
            databaseService.addCourse(newCourse);

            // Update UI
            refreshCourseList();
            clearCourseFields();
            showAlert("Success", "Course Added Successfully!");

        } catch (NumberFormatException e) {
            showAlert("Error", "Credits must be a number.");
        }
    }

    // Action to add a new routine slot
    @FXML
    private void onAddRoutineClick() {
        String day = daySelector.getValue();
        String time = timeField.getText();
        String room = roomField.getText();
        String section = sectionField.getText();
        Course selectedCourse = courseSelector.getValue();
        String selectedTeacher = teacherSelector.getValue();

        if (time.isEmpty() || room.isEmpty() || section.isEmpty() || selectedCourse == null || selectedTeacher == null) {
            showAlert("Error", "Please fill all routine fields.");
            return;
        }

        // Create new RoutineSlot object
        RoutineSlot newSlot = new RoutineSlot(day, time, room, selectedCourse, selectedTeacher, section);

        // Save to database
        databaseService.addRoutineSlot(newSlot);

        showAlert("Success", "Routine Slot Added for " + section);
    }


    // HELPER METHODS

    // Load users into the TableView
    private void loadUserData() {
        ObservableList<User> users = FXCollections.observableArrayList(databaseService.getUserList());
        userTableView.setItems(users);
    }

    // Filter teachers from user list and populate the selector
    private void loadTeachersIntoSelector() {
        teacherSelector.getItems().clear();
        for (User user : databaseService.getUserList()) {
            if (user instanceof Teacher) {
                teacherSelector.getItems().add(user.getFullName());
            }
        }
    }

    // Refresh course lists in ListView and ComboBox
    private void refreshCourseList() {
        // Update ListView
        ObservableList<Course> courses = FXCollections.observableArrayList(databaseService.getCourseList());
        courseListView.setItems(courses);

        // Update Course Selector for Routine
        courseSelector.setItems(courses);
    }

    // Clear input fields for User Management
    private void clearUserFields() {
        userIdField.clear(); passwordField.clear(); fullNameField.clear(); detailsField.clear();
    }

    // Clear input fields for Course Management
    private void clearCourseFields() {
        courseCodeField.clear(); courseTitleField.clear(); creditField.clear();
    }

    // Helper method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Logout Action
    @FXML
    private void onLogoutButtonClick() {
        try {
            // 1. Load the Login.fxml scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/university/view/Login.fxml"));
            Scene scene = new Scene(loader.load());

            // 2. Get the current Stage (Window)
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();

            // 3. Set the new Scene to the Stage
            currentStage.setScene(scene);
            currentStage.setTitle("ClassPilot Login");
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load login screen.");
        }
    }
}