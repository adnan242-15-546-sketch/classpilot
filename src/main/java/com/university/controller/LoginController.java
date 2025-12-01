package com.university.controller;

import com.university.model.Admin;
import com.university.model.DatabaseService;
import com.university.model.User;
import com.university.model.Student;
import com.university.model.Teacher;
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

    @FXML private TextField userIdField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleSelector;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private DatabaseService databaseService = new DatabaseService();

    @FXML
    public void initialize() {
        // Populate role selector
        roleSelector.getItems().addAll("Student", "Teacher", "Admin");
        roleSelector.setValue("Admin");

        // Create a default admin if the database is empty
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

        // Basic validation
        if (userId.isEmpty() || password.isEmpty() || role == null) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        // Validate credentials against the database
        User user = validateLogin(userId, password, role);

        if (user != null) {
            openDashboard(user);
        } else {
            errorLabel.setText("Invalid User ID, Password, or Role.");
        }
    }

    // Helper method to validate user credentials
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

    // Method to handle dashboard navigation based on user role
    private void openDashboard(User user) {
        try {
            String fxmlFile = null;
            String title = null;

            FXMLLoader loader = null;
            Stage dashboardStage = new Stage();

            // Determine FXML file based on role
            if (user.getRole().equals("Admin")) {
                fxmlFile = "/com/university/view/AdminDashboard.fxml";
                title = "Admin Dashboard";

            } else if (user.getRole().equals("Student")) {
                fxmlFile = "/com/university/view/StudentDashboard.fxml";
                title = "Student Dashboard";

                loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Scene scene = new Scene(loader.load());

                // Pass student data to the controller
                StudentDashboardController controller = loader.getController();
                controller.initData((Student) user);

                dashboardStage.setTitle(title);
                dashboardStage.setScene(scene);
                dashboardStage.show();

                // Close login window
                closeLoginWindow();
                return;

            } else if (user.getRole().equals("Teacher")) {
                fxmlFile = "/com/university/view/TeacherDashboard.fxml";
                title = "Teacher Dashboard";

                loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Scene scene = new Scene(loader.load());

                // Pass teacher data to the controller
                TeacherDashboardController controller = loader.getController();
                controller.initData((Teacher) user);

                dashboardStage.setTitle(title);
                dashboardStage.setScene(scene);
                dashboardStage.show();

                // Close login window
                closeLoginWindow();
                return;
            }

            // Fallback loading logic for Admin (since it didn't return early)
            if(fxmlFile != null){
                loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Scene scene = new Scene(loader.load());

                dashboardStage.setTitle(title);
                dashboardStage.setScene(scene);
                dashboardStage.show();

                // Close login window
                closeLoginWindow();
            }

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to open dashboard. (Check console)");
        } catch (ClassCastException e) {
            e.printStackTrace();
            errorLabel.setText("System Error: User type mismatch.");
        }
    }

    // Helper method to close the current login stage
    private void closeLoginWindow() {
        Stage loginStage = (Stage) loginButton.getScene().getWindow();
        loginStage.close();
    }
}