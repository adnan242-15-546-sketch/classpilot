package com.university.controller;

import com.university.model.Admin;
import com.university.model.DatabaseService;
import com.university.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField userIdField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ChoiceBox<String> roleSelector;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;

    private DatabaseService databaseService = new DatabaseService();

    @FXML
    public void initialize() {
        roleSelector.getItems().addAll("Student", "Teacher", "Admin");
        roleSelector.setValue("Admin");

        if (databaseService.getUserList().isEmpty()) {
            Admin defaultAdmin = new Admin("admin", "123", "Default Admin");
            databaseService.addUser(defaultAdmin);
            System.out.println("No users found. Default admin created.");
        }
    }

    @FXML
    private void onLoginButtonClick() {
        String userId = userIdField.getText();
        String password = passwordField.getText();
        String role = roleSelector.getValue();

        if (userId.isEmpty() || password.isEmpty() || role == null) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        User user = validateLogin(userId, password, role);

        if (user != null) {

            openDashboard(user);

        } else {
            errorLabel.setText("Invalid User ID, Password, or Role.");
        }
    }

    private User validateLogin(String userId, String password, String role) {
        for (User user : databaseService.getUserList()) {
            if (user.getUserId().equals(userId) &&
                    user.validatePassword(password) &&
                    user.getRole().equals(role))
            {
                return user;
            }
        }
        return null;
    }


    private void openDashboard(User user) {
        try {
            String fxmlFile;
            String title;


            if (user.getRole().equals("Admin")) {
                fxmlFile = "/com/university/view/AdminDashboard.fxml";
                title = "Admin Dashboard";
            } else if (user.getRole().equals("Student")) {

                errorLabel.setText("Student dashboard not ready yet!");
                return;
            } else {

                errorLabel.setText("Teacher dashboard not ready yet!");
                return;
            }


            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load()); // নতুন সিন লোড করা


            Stage dashboardStage = new Stage();
            dashboardStage.setTitle(title);
            dashboardStage.setScene(scene);



            dashboardStage.show();


            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to open dashboard.");
        }
    }
}