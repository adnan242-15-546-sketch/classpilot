package com.university;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class VMSApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(VMSApp.class.getResource("/com/university/view/Login.fxml"));


        Scene scene = new Scene(fxmlLoader.load(), 800, 600);


        stage.setTitle("ClassPilot");

        stage.setScene(scene);
        stage.show();
    }
}