package com.university;

import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class VMSApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // 1. Theme Setup
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        // 2. Load Login Screen
        FXMLLoader fxmlLoader = new FXMLLoader(VMSApp.class.getResource("/com/university/view/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

        // --- APP ICON SETUP (Taskbar & Title Bar) ---
        try {
            Image appIcon = new Image(Objects.requireNonNull(VMSApp.class.getResourceAsStream("/com/university/view/classpilot.png")));
            stage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.out.println("Warning: Logo not found! Check if logo.png exists in resources.");
        }
        // -----------------------------------------------

        stage.setTitle("ClassPilot");
        stage.setScene(scene);
        stage.show();
    }
}