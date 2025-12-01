package com.university.controller;

import com.university.model.DatabaseService;
import com.university.model.RoutineSlot;
import com.university.model.Student;
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

public class StudentDashboardController {


    // FXML COMPONENTS (Routine Tab)
    @FXML private TableView<RoutineSlot> routineTable;
    @FXML private TableColumn<RoutineSlot, String> dayCol;
    @FXML private TableColumn<RoutineSlot, String> timeCol;
    @FXML private TableColumn<RoutineSlot, String> subjectCol;
    @FXML private TableColumn<RoutineSlot, String> roomCol;


    // FXML COMPONENTS (Chat Tab)
    @FXML private ComboBox<String> chatCourseSelector; // Selector for course groups
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button logoutButton;


    // DATA AND SERVICES
    private Student currentStudent;
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
            chatArea.setText("Please select a course group to start chatting.\n");
        }

        // Setup Routine Table columns using Lambda expressions
        dayCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDay()));
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNo()));
        subjectCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseInfo()));
    }

    // Method called from LoginController to pass student data
    public void initData(Student student) {
        this.currentStudent = student;
        System.out.println("Dashboard Loaded for: " + student.getFullName());

        // 1. Load routine for the student's section
        loadRoutine(student.getBatch());

        // 2. Load available chat groups based on enrolled courses
        loadChatGroups(student.getBatch());
    }


    // CHAT GROUP LOGIC

    private void loadChatGroups(String section) {
        // Retrieve unique course codes from the routine for the specific section
        List<String> myCourses = databaseService.getRoutineList().stream()
                .filter(slot -> slot.getSection().equals(section)) // Filter by section
                .map(slot -> slot.getCourse().getCourseCode()) // Map to course code
                .distinct() // Remove duplicates
                .collect(Collectors.toList());

        // Add groups to the ComboBox
        chatCourseSelector.getItems().add("General"); // Default general group
        chatCourseSelector.getItems().addAll(myCourses);

        // Set default selection
        chatCourseSelector.setValue("General");

        // Listener for dropdown selection change
        chatCourseSelector.setOnAction(event -> {
            String selectedGroup = chatCourseSelector.getValue();
            if (selectedGroup != null) {
                // Switch connection to the new group in a separate thread
                new Thread(() -> connectToChatServer(selectedGroup)).start();
            }
        });

        // Automatically join 'General' group on startup
        new Thread(() -> connectToChatServer("General")).start();
    }


    // NETWORKING METHODS (Dynamic Room Switching)

    private void connectToChatServer(String roomSuffix) {
        try {
            // 1. Close existing connection if any
            isRunning = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            // 2. Update UI
            Platform.runLater(() -> {
                chatArea.clear();
                chatArea.appendText("Connecting to " + roomSuffix + " Group...\n");
            });

            // 3. Establish new connection
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            isRunning = true;

            // 4. Generate unique group name (e.g., "67_A_CSE101")
            // This ensures students from other sections cannot join this group
            String uniqueGroupName = currentStudent.getBatch() + "_" + roomSuffix;

            // 5. Send JOIN request to server
            writer.println("JOIN:" + uniqueGroupName + ":" + currentStudent.getFullName());

            // 6. Loop to listen for incoming messages
            String incomingMessage;
            while (isRunning && (incomingMessage = reader.readLine()) != null) {
                String finalMsg = incomingMessage;
                Platform.runLater(() -> chatArea.appendText(finalMsg + "\n"));
            }

        } catch (IOException e) {
            if (isRunning) { // Only show error if connection wasn't closed manually
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

    private void loadRoutine(String studentSection) {
        List<RoutineSlot> allSlots = databaseService.getRoutineList();

        // Filter routine slots by student's section
        List<RoutineSlot> filteredSlots = allSlots.stream()
                .filter(slot -> slot.getSection().equals(studentSection))
                .collect(Collectors.toList());

        ObservableList<RoutineSlot> observableList = FXCollections.observableArrayList(filteredSlots);
        routineTable.setItems(observableList);
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