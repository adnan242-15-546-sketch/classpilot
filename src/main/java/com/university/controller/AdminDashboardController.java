package com.university.controller;

import com.university.model.*; // User, Student, Teacher, Admin
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminDashboardController {

    // --- FXML
    @FXML
    private TextField userIdField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private ChoiceBox<String> roleSelector;
    @FXML
    private TextField detailsField;
    @FXML
    private Button createUserButton;
    @FXML
    private Label statusLabel;

    // TableView
    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;


    private DatabaseService databaseService = new DatabaseService();


    @FXML
    public void initialize() {

        roleSelector.getItems().addAll("Student", "Teacher");
        roleSelector.setValue("Student");


        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserId()));

        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));

        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));

        loadUserData();
    }


    @FXML
    private void onCreateUserButtonClick() {
        String userId = userIdField.getText();
        String password = passwordField.getText();
        String fullName = fullNameField.getText();
        String role = roleSelector.getValue();
        String details = detailsField.getText();


        if (userId.isEmpty() || password.isEmpty() || fullName.isEmpty() || role == null || details.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }


        User newUser;
        if (role.equals("Student")) {
            // details = batch
            newUser = new Student(userId, password, fullName, details);
        } else {
            // details = designation
            newUser = new Teacher(userId, password, fullName, details);
        }


        databaseService.addUser(newUser);

        statusLabel.setText(role + " created successfully!");

        // TableView
        loadUserData();


        clearInputFields();
    }


    private void loadUserData() {

        ObservableList<User> userList = FXCollections.observableArrayList(databaseService.getUserList());
        userTableView.setItems(userList);
    }


    private void clearInputFields() {
        userIdField.clear();
        passwordField.clear();
        fullNameField.clear();
        detailsField.clear();
    }
}