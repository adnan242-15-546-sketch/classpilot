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

    // ==========================
    // FXML COMPONENTS
    // ==========================
    @FXML private TextField userIdField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleSelector;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    // ==========================
    // DATA & SERVICES
    // ==========================
    private DatabaseService databaseService = new DatabaseService();

    @FXML
    public void initialize() {
        // Populate role selector
        roleSelector.getItems().addAll("Student", "Teacher", "Admin");
        roleSelector.setValue("Admin");

        // Create a default admin if the database is empty (First-time setup)
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

    // Method to handle dashboard navigation (Seamless Transition)
    private void openDashboard(User user) {
        try {
            String fxmlFile = null;
            String title = null;

            // Determine FXML file and Title based on role
            if (user.getRole().equals("Admin")) {
                fxmlFile = "/com/university/view/AdminDashboard.fxml";
                title = "ClassPilot - Admin Dashboard";
            } else if (user.getRole().equals("Student")) {
                fxmlFile = "/com/university/view/StudentDashboard.fxml";
                title = "ClassPilot - Student Dashboard";
            } else if (user.getRole().equals("Teacher")) {
                fxmlFile = "/com/university/view/TeacherDashboard.fxml";
                title = "ClassPilot - Teacher Dashboard";
            }

            if (fxmlFile == null) return;

            // 1. Get current stage and dimensions to maintain window size
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            double currentWidth = currentStage.getScene().getWidth();
            double currentHeight = currentStage.getScene().getHeight();

            // 2. Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));

            // 3. Create new Scene with the previous dimensions (Seamless feel)
            Scene scene = new Scene(loader.load(), currentWidth, currentHeight);

            // 4. Pass user data to the specific controller
            if (user.getRole().equals("Student")) {
                StudentDashboardController controller = loader.getController();
                controller.initData((Student) user);
            } else if (user.getRole().equals("Teacher")) {
                TeacherDashboardController controller = loader.getController();
                controller.initData((Teacher) user);
            }

            // 5. Set the new scene to the current stage
            currentStage.setTitle(title);
            currentStage.setScene(scene);

            // Note: centerOnScreen() is removed to prevent the window from jumping.

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load dashboard.");
        }
    }
}