package com.university.controller;

import com.university.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AdminDashboardController {

    // ==========================
    // FXML COMPONENTS (Tab 1: User Management)
    // ==========================
    @FXML private TextField userIdField;
    @FXML private TextField passwordField;
    @FXML private TextField fullNameField;
    @FXML private ChoiceBox<String> roleSelector;
    @FXML private TextField detailsField;
    @FXML private Label statusLabel;

    @FXML private Button createUserButton; // Used for both Create and Update
    @FXML private Button clearUserButton;
    @FXML private Button deleteUserButton;

    // User Table Components
    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, String> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> roleColumn;

    // ==========================
    // FXML COMPONENTS (Tab 2: Routine Management)
    // ==========================

    // Section A: Course Management
    @FXML private TextField courseCodeField;
    @FXML private TextField courseTitleField;
    @FXML private TextField creditField;
    @FXML private ListView<Course> courseListView;

    // Section B: Routine Creation Form
    @FXML private ChoiceBox<String> daySelector;
    @FXML private TextField timeField;
    @FXML private TextField roomField;
    @FXML private TextField sectionField;
    @FXML private ChoiceBox<Course> courseSelector;
    @FXML private ChoiceBox<String> teacherSelector;

    // Section C: All Routines Table
    @FXML private TableView<RoutineSlot> allRoutineTable;
    @FXML private TableColumn<RoutineSlot, String> rSectionCol;
    @FXML private TableColumn<RoutineSlot, String> rDayCol;
    @FXML private TableColumn<RoutineSlot, String> rTimeCol;
    @FXML private TableColumn<RoutineSlot, String> rCourseCol;
    @FXML private TableColumn<RoutineSlot, String> rTeacherCol;

    // ==========================
    // SIDEBAR & NAVIGATION
    // ==========================
    @FXML private TabPane mainTabPane;
    @FXML private Button logoutButton;

    // ==========================
    // DATA & STATE
    // ==========================
    private DatabaseService databaseService = new DatabaseService();
    private User selectedUserForUpdate = null; // Tracks user selection for updates

    @FXML
    public void initialize() {
        // --- Tab 1 Initialization ---
        roleSelector.getItems().addAll("Student", "Teacher");
        roleSelector.setValue("Student");

        // Setup User Table Columns
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        // Listener: Populate form when a user row is selected
        userTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateUserForm(newSelection);
            }
        });

        loadUserData();

        // --- Tab 2 Initialization ---
        daySelector.getItems().addAll("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        daySelector.setValue("Sunday");

        // Setup Routine Table Columns
        rSectionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSection()));
        rDayCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDay()));
        rTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
        rCourseCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourse().getCourseCode()));
        rTeacherCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTeacherName()));

        loadTeachersIntoSelector();
        refreshCourseList();
        refreshRoutineList();
    }

    // ==========================
    // TAB 1 ACTIONS (User CRUD)
    // ==========================

    @FXML
    private void onCreateUserButtonClick() {
        String userId = userIdField.getText();
        String password = passwordField.getText();
        String fullName = fullNameField.getText();
        String role = roleSelector.getValue();
        String details = detailsField.getText();

        if (userId.isEmpty() || password.isEmpty() || fullName.isEmpty() || details.isEmpty()) {
            statusLabel.setText("Fill all fields!");
            return;
        }

        // Create user object based on role
        User userObj;
        if (role.equals("Student")) {
            userObj = new Student(userId, password, fullName, details);
        } else {
            userObj = new Teacher(userId, password, fullName, details);
        }

        if (selectedUserForUpdate != null) {
            // Update existing user
            databaseService.updateUser(selectedUserForUpdate, userObj);
            statusLabel.setText("User Updated!");
        } else {
            // Create new user
            databaseService.addUser(userObj);
            statusLabel.setText("User Created!");
        }

        loadUserData();
        loadTeachersIntoSelector(); // Refresh teacher list for routine tab
        onClearUserSelection(); // Reset form
    }

    @FXML
    private void onDeleteUserClick() {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a user to delete.");
            return;
        }

        if (confirmAction("Delete User", "Are you sure you want to delete " + selected.getFullName() + "?")) {
            databaseService.deleteUser(selected);
            loadUserData();
            loadTeachersIntoSelector();
            onClearUserSelection();
        }
    }

    @FXML
    private void onClearUserSelection() {
        userIdField.clear();
        passwordField.clear();
        fullNameField.clear();
        detailsField.clear();
        statusLabel.setText("");

        selectedUserForUpdate = null;
        userTableView.getSelectionModel().clearSelection();
        createUserButton.setText("Create User Account"); // Reset button text
    }

    private void populateUserForm(User user) {
        selectedUserForUpdate = user;
        userIdField.setText(user.getUserId());
        // Ideally, password shouldn't be shown, but keeping it for simplicity
        // passwordField.setText("");
        fullNameField.setText(user.getFullName());
        roleSelector.setValue(user.getRole());

        if (user instanceof Student) {
            detailsField.setText(((Student) user).getBatch());
        } else if (user instanceof Teacher) {
            detailsField.setText(((Teacher) user).getDesignation());
        }

        createUserButton.setText("Update User"); // Change button text to indicate update mode
    }

    // ==========================
    // TAB 2 ACTIONS (Routine & Course)
    // ==========================

    @FXML
    private void onAddCourseClick() {
        String code = courseCodeField.getText();
        String title = courseTitleField.getText();
        String creditStr = creditField.getText();

        if (code.isEmpty() || title.isEmpty() || creditStr.isEmpty()) return;

        try {
            double credits = Double.parseDouble(creditStr);
            Course newCourse = new Course(code, title, credits);
            databaseService.addCourse(newCourse);

            refreshCourseList();
            clearCourseFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Credits must be a number.");
        }
    }

    @FXML
    private void onDeleteCourseClick() {
        Course selected = courseListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (confirmAction("Delete Course", "Delete " + selected.getCourseCode() + "?")) {
            databaseService.deleteCourse(selected);
            refreshCourseList();
        }
    }

    @FXML
    private void onAddRoutineClick() {
        String day = daySelector.getValue();
        String time = timeField.getText();
        String room = roomField.getText();
        String section = sectionField.getText();
        Course selectedCourse = courseSelector.getValue();
        String selectedTeacher = teacherSelector.getValue();

        if (time.isEmpty() || room.isEmpty() || section.isEmpty() || selectedCourse == null || selectedTeacher == null) {
            showAlert("Error", "Fill fields.");
            return;
        }

        RoutineSlot newSlot = new RoutineSlot(day, time, room, selectedCourse, selectedTeacher, section);
        databaseService.addRoutineSlot(newSlot);

        showAlert("Success", "Routine Slot Added!");
        refreshRoutineList();
    }

    @FXML
    private void onDeleteRoutineClick() {
        RoutineSlot selected = allRoutineTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a routine slot to delete.");
            return;
        }

        if (confirmAction("Delete Slot", "Remove " + selected.getCourse().getCourseCode() + " class?")) {
            databaseService.deleteRoutine(selected);
            refreshRoutineList();
        }
    }

    // ==========================
    // HELPER METHODS
    // ==========================

    private void loadUserData() {
        ObservableList<User> users = FXCollections.observableArrayList(databaseService.getUserList());
        userTableView.setItems(users);
    }

    private void refreshCourseList() {
        ObservableList<Course> courses = FXCollections.observableArrayList(databaseService.getCourseList());
        courseListView.setItems(courses);
        courseSelector.setItems(courses);
    }

    private void refreshRoutineList() {
        ObservableList<RoutineSlot> slots = FXCollections.observableArrayList(databaseService.getRoutineList());
        allRoutineTable.setItems(slots);
    }

    private void loadTeachersIntoSelector() {
        teacherSelector.getItems().clear();
        for (User user : databaseService.getUserList()) {
            if (user instanceof Teacher) {
                teacherSelector.getItems().add(user.getFullName());
            }
        }
    }

    private void clearUserFields() {
        userIdField.clear(); passwordField.clear(); fullNameField.clear(); detailsField.clear();
    }

    private void clearCourseFields() {
        courseCodeField.clear(); courseTitleField.clear(); creditField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmAction(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Navigation & Logout
    @FXML private void switchTabUsers() { mainTabPane.getSelectionModel().select(0); }
    @FXML private void switchTabRoutine() { mainTabPane.getSelectionModel().select(1); }

    @FXML
    private void onLogoutButtonClick() {
        try {
            // Admin doesn't have a chat socket, so no need to close it.

            // Load Login Scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/university/view/Login.fxml"));

            // Get current stage and size for seamless transition
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            double width = currentStage.getScene().getWidth();
            double height = currentStage.getScene().getHeight();

            Scene scene = new Scene(loader.load(), width, height);

            currentStage.setScene(scene);
            currentStage.setTitle("ClassPilot Login");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load login screen.");
        }
    }
}