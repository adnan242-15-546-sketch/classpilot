package com.university.controller;

import com.university.model.DatabaseService;
import com.university.model.RoutineSlot;
import com.university.model.Student;
import com.university.model.Notice;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StudentDashboardController {

    // ==========================
    // SIDEBAR & NAVIGATION
    // ==========================
    @FXML private TabPane mainTabPane;
    @FXML private Button logoutButton;

    // ==========================
    // ROUTINE TAB COMPONENTS
    // ==========================
    @FXML private FlowPane routineContainer;
    @FXML private CheckBox todayOnlyCheckBox;

    // ==========================
    // CHAT TAB COMPONENTS
    // ==========================
    @FXML private ComboBox<String> chatCourseSelector;
    @FXML private VBox chatContainer;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    // ==========================
    // NOTICE TAB COMPONENTS
    // ==========================
    @FXML private VBox noticeContainer;

    // ==========================
    // DATA & SERVICES
    // ==========================
    private Student currentStudent;
    private DatabaseService databaseService = new DatabaseService();

    // ==========================
    // NETWORKING VARIABLES
    // ==========================
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private volatile boolean isRunning = false;

    @FXML
    public void initialize() {
        // Routine filtering action
        if (todayOnlyCheckBox != null) {
            todayOnlyCheckBox.setOnAction(event -> loadRoutine(currentStudent.getBatch()));
        }
    }

    public void initData(Student student) {
        this.currentStudent = student;
        System.out.println("Dashboard Loaded for: " + student.getFullName());

        loadRoutine(student.getBatch());
        loadChatGroups(student.getBatch());
        loadNotices(student.getBatch());
    }

    // ==========================
    // NAVIGATION METHODS
    // ==========================
    @FXML private void switchTabRoutine() { mainTabPane.getSelectionModel().select(0); }
    @FXML private void switchTabChat() { mainTabPane.getSelectionModel().select(1); }
    @FXML private void switchTabNotice() { mainTabPane.getSelectionModel().select(2); }

    // ==========================
    // NOTICE LOADING LOGIC (CARD VIEW)
    // ==========================
    private void loadNotices(String studentSection) {
        noticeContainer.getChildren().clear();

        // 1. Filter notices by section or ALL
        List<Notice> relevantNotices = databaseService.getNoticeList().stream()
                .filter(n -> n.getTargetSection().equals(studentSection) || n.getTargetSection().equals("ALL SECTIONS"))
                .collect(Collectors.toList());

        // 2. Empty state
        if (relevantNotices.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(50));
            Label titleLabel = new Label("No new notices right now.");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #999;");
            emptyState.getChildren().add(titleLabel);
            noticeContainer.getChildren().add(emptyState);
            return;
        }

        // 3. Generate notice cards (Reverse order for latest first)
        for (int i = relevantNotices.size() - 1; i >= 0; i--) {
            Notice notice = relevantNotices.get(i);

            VBox card = new VBox();
            card.getStyleClass().add("notice-card");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label titleLbl = new Label(notice.getTitle());
            titleLbl.getStyleClass().add("notice-title");
            HBox.setHgrow(titleLbl, javafx.scene.layout.Priority.ALWAYS);
            titleLbl.setMaxWidth(Double.MAX_VALUE);

            Label badgeLbl = new Label(notice.getTargetSection());
            badgeLbl.getStyleClass().add("notice-badge");

            header.getChildren().addAll(titleLbl, badgeLbl);

            Label contentLbl = new Label(notice.getContent());
            contentLbl.getStyleClass().add("notice-content");
            contentLbl.setWrapText(true);

            HBox footer = new HBox(15);
            footer.getStyleClass().add("notice-footer");

            Label senderLbl = new Label("✍ " + notice.getSenderName());
            Label timeLbl = new Label("🕒 " + notice.getTimestamp());

            footer.getChildren().addAll(senderLbl, timeLbl);

            card.getChildren().addAll(header, contentLbl, new Separator(), footer);
            noticeContainer.getChildren().add(card);
        }
    }

    // ==========================
    // ROUTINE LOGIC (CARD VIEW)
    // ==========================
    private void loadRoutine(String studentSection) {
        routineContainer.getChildren().clear();
        List<RoutineSlot> allSlots = databaseService.getRoutineList();

        // 1. Filter by Section
        List<RoutineSlot> filteredSlots = allSlots.stream()
                .filter(slot -> slot.getSection().equals(studentSection))
                .collect(Collectors.toList());

        // 2. Filter by Today (if selected)
        if (todayOnlyCheckBox.isSelected()) {
            String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            filteredSlots = filteredSlots.stream()
                    .filter(slot -> slot.getDay().equalsIgnoreCase(today))
                    .collect(Collectors.toList());
        }

        // 3. Empty State
        if (filteredSlots.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(50));

            Label titleLabel;
            if (todayOnlyCheckBox.isSelected()) {
                titleLabel = new Label("Relax! No classes today. 🎉");
                titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
            } else {
                titleLabel = new Label("No routine found for your section.");
                titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");
            }
            emptyState.getChildren().add(titleLabel);
            routineContainer.getChildren().add(emptyState);
            return;
        }

        // 4. Generate Cards
        for (RoutineSlot slot : filteredSlots) {
            VBox card = new VBox(8);
            card.getStyleClass().add("routine-card");

            Label courseCode = new Label(slot.getCourse().getCourseCode());
            courseCode.getStyleClass().add("card-title");

            Label courseTitle = new Label(slot.getCourse().getCourseTitle());
            courseTitle.setWrapText(true);
            courseTitle.getStyleClass().add("card-subtitle");

            card.getChildren().addAll(courseCode, courseTitle, new Separator(),
                    new Label("🕒 " + slot.getDay() + " @ " + slot.getTime()),
                    new Label("📍 " + slot.getRoomNo()),
                    new Label("👨‍🏫 " + slot.getTeacherName()));

            routineContainer.getChildren().add(card);
        }
    }

    // ==========================
    // CHAT LOGIC
    // ==========================
    private void loadChatGroups(String section) {
        List<String> myCourses = databaseService.getRoutineList().stream()
                .filter(slot -> slot.getSection().equals(section))
                .map(slot -> slot.getCourse().getCourseCode())
                .distinct()
                .collect(Collectors.toList());

        chatCourseSelector.getItems().add("General");
        chatCourseSelector.getItems().addAll(myCourses);
        chatCourseSelector.setValue("General");

        chatCourseSelector.setOnAction(event -> {
            String selectedGroup = chatCourseSelector.getValue();
            if (selectedGroup != null) {
                new Thread(() -> connectToChatServer(selectedGroup)).start();
            }
        });
        new Thread(() -> connectToChatServer("General")).start();
    }

    private void addMessageToChat(String sender, String message, boolean isMe) {
        Platform.runLater(() -> {
            Text text = new Text(message);
            text.getStyleClass().add(isMe ? "chat-text-me" : "chat-text-other");
            TextFlow textFlow = new TextFlow(text);
            textFlow.getStyleClass().add(isMe ? "chat-bubble-me" : "chat-bubble-other");
            textFlow.setMaxWidth(300);

            VBox chatBox = new VBox(2);
            if (!isMe) {
                Label senderLabel = new Label(sender);
                senderLabel.getStyleClass().add("sender-name");
                chatBox.getChildren().add(senderLabel);
            }
            chatBox.getChildren().add(textFlow);

            HBox container = new HBox();
            container.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            container.getChildren().add(chatBox);
            chatContainer.getChildren().add(container);
        });
    }

    private void connectToChatServer(String roomSuffix) {
        try {
            isRunning = false;
            if (socket != null && !socket.isClosed()) socket.close();

            Platform.runLater(() -> {
                chatContainer.getChildren().clear();
                Label sysMsg = new Label("Connecting to " + roomSuffix + "...");
                sysMsg.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
                HBox centerBox = new HBox(sysMsg);
                centerBox.setAlignment(Pos.CENTER);
                chatContainer.getChildren().add(centerBox);
            });

            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            isRunning = true;

            String uniqueGroupName = currentStudent.getBatch() + "_" + roomSuffix;
            writer.println("JOIN:" + uniqueGroupName + ":" + currentStudent.getFullName());

            String incomingMessage;
            while (isRunning && (incomingMessage = reader.readLine()) != null) {
                String finalMsg = incomingMessage;
                if(finalMsg.contains(":")) {
                    String[] parts = finalMsg.split(":", 2);
                    boolean isMe = parts[0].trim().equals(currentStudent.getFullName());
                    addMessageToChat(parts[0].trim(), parts[1].trim(), isMe);
                } else {
                    Platform.runLater(() -> {
                        Label sysLabel = new Label(finalMsg);
                        sysLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");
                        HBox centerBox = new HBox(sysLabel);
                        centerBox.setAlignment(Pos.CENTER);
                        chatContainer.getChildren().add(centerBox);
                    });
                }
            }
        } catch (IOException e) {
            if (isRunning) Platform.runLater(() -> chatContainer.getChildren().add(new Label("Connection Lost.")));
        }
    }

    @FXML private void onSendButtonClick() {
        String msg = messageField.getText();
        if (!msg.isEmpty() && writer != null) {
            writer.println("MSG:" + msg);
            writer.flush();
            messageField.clear();
        }
    }

    // ==========================
    // LOGOUT LOGIC
    // ==========================
    @FXML private void onLogoutButtonClick() {
        try {
            isRunning = false;
            if(socket != null && !socket.isClosed()) socket.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/university/view/Login.fxml"));
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            double width = currentStage.getScene().getWidth();
            double height = currentStage.getScene().getHeight();

            Scene scene = new Scene(loader.load(), width, height);
            currentStage.setScene(scene);
            currentStage.setTitle("ClassPilot Login");
        } catch (IOException e) { e.printStackTrace(); }
    }
}