package com.university.controller;

import com.university.model.DatabaseService;
import com.university.model.RoutineSlot;
import com.university.model.Teacher;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherDashboardController {


    // FXML COMPONENTS (Routine Tab)
    @FXML private TableView<RoutineSlot> teacherRoutineTable;
    @FXML private TableColumn<RoutineSlot, String> dayCol;
    @FXML private TableColumn<RoutineSlot, String> timeCol;
    @FXML private TableColumn<RoutineSlot, String> subjectCol;
    @FXML private TableColumn<RoutineSlot, String> roomCol;


    // FXML COMPONENTS (Chat Tab)
    @FXML private ComboBox<String> chatCourseSelector; // Selector for class groups
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button logoutButton;


    // DATA AND SERVICES

    private Teacher currentTeacher;
    private DatabaseService databaseService = new DatabaseService();


    // NETWORKING VARIABLES
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    // Flag to control connection state
    private volatile boolean isRunning = false;

    @FXML
    public void initialize() {
        // Initialize chat area
        if(chatArea != null) {
            chatArea.setEditable(false);
            chatArea.setText("Please select a class group to start chatting.\n");
        }

        // Setup Routine Table columns using Lambda expressions
        dayCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDay()));
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
        subjectCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseInfo()));
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNo()));
    }

    // Method called from LoginController to pass teacher data
    public void initData(Teacher teacher) {
        this.currentTeacher = teacher;
        System.out.println("Dashboard Loaded for: " + teacher.getFullName());

        // 1. Load routine for the teacher
        loadRoutine(teacher.getFullName());

        // 2. Load available chat groups based on assigned classes
        loadTeacherChatGroups(teacher.getFullName());
    }


    // CHAT GROUP LOGIC


    private void loadTeacherChatGroups(String teacherName) {
        // Retrieve routine slots for this teacher and map them to "Section_CourseCode" format
        // Example: "67_A_CSE101"
        List<String> myClasses = databaseService.getRoutineList().stream()
                .filter(slot -> slot.getTeacherName().equals(teacherName))
                .map(slot -> slot.getSection() + "_" + slot.getCourse().getCourseCode())
                .distinct() // Remove duplicates
                .collect(Collectors.toList());

        if (myClasses.isEmpty()) {
            chatCourseSelector.setPromptText("No Assigned Classes");
            chatArea.setText("You have no assigned classes/groups yet.");
            return;
        }

        // Add class groups to the ComboBox
        chatCourseSelector.getItems().addAll(myClasses);

        // Automatically select the first class and connect
        if (!myClasses.isEmpty()) {
            chatCourseSelector.setValue(myClasses.get(0));
            new Thread(() -> connectToChatServer(myClasses.get(0))).start();
        }

        // Listener for dropdown selection change
        chatCourseSelector.setOnAction(event -> {
            String selectedGroup = chatCourseSelector.getValue();
            if (selectedGroup != null) {
                // Switch connection to the new group in a separate thread
                new Thread(() -> connectToChatServer(selectedGroup)).start();
            }
        });
    }


    // NETWORKING METHODS (Dynamic Room Switching)

    private void connectToChatServer(String uniqueRoomName) {
        try {
            // 1. Close existing connection if any
            isRunning = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            // 2. Update UI
            Platform.runLater(() -> {
                chatArea.clear();
                chatArea.appendText("Joining Group: " + uniqueRoomName + "...\n");
            });

            // 3. Establish new connection
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            isRunning = true;

            // 4. Send JOIN request to server as a Teacher
            // The room name matches the selection (e.g., "67_A_CSE101")
            writer.println("JOIN:" + uniqueRoomName + ":" + currentTeacher.getFullName() + " (Teacher)");

            // 5. Loop to listen for incoming messages
            String incomingMessage;
            while (isRunning && (incomingMessage = reader.readLine()) != null) {
                String finalMsg = incomingMessage;
                Platform.runLater(() -> chatArea.appendText(finalMsg + "\n"));
            }

        } catch (IOException e) {
            if(isRunning) {
                Platform.runLater(() -> chatArea.appendText("Connection closed or failed.\n"));
            }
        }
    }

    @FXML
    private void onSendButtonClick() {
        String msg = messageField.getText();
        if (!msg.isEmpty() && writer != null) {
            // Send message to server
            writer.println("MSG:" + msg);
            writer.flush(); // Crucial: Flush the stream to ensure message is sent immediately
            messageField.clear();
        }
    }


    // ROUTINE LOGIC

    private void loadRoutine(String teacherFullName) {
        List<RoutineSlot> allSlots = databaseService.getRoutineList();

        // Filter routine slots by teacher's name
        List<RoutineSlot> filteredSlots = allSlots.stream()
                .filter(slot -> slot.getTeacherName().equals(teacherFullName))
                .collect(Collectors.toList());

        ObservableList<RoutineSlot> observableList = FXCollections.observableArrayList(filteredSlots);
        teacherRoutineTable.setItems(observableList);
    }


    // LOGOUT LOGIC

    @FXML
    private void onLogoutButtonClick() {
        try {
            // Close chat connection
            isRunning = false;
            if(socket != null && !socket.isClosed()) socket.close();

            // Return to login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/university/view/Login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("ClassPilot Login");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}