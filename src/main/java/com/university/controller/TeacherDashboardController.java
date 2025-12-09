package com.university.controller;

import com.university.model.DatabaseService;
import com.university.model.Notice;
import com.university.model.RoutineSlot;
import com.university.model.Teacher;
import javafx.application.Platform;
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
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeacherDashboardController {

    // ==========================
    // SIDEBAR & NAVIGATION
    // ==========================
    @FXML private TabPane mainTabPane;
    @FXML private Button logoutButton;

    // ==========================
    // ROUTINE TAB COMPONENTS
    // ==========================
    @FXML private FlowPane teacherRoutineContainer;
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
    @FXML private ComboBox<String> noticeTargetSelector;
    @FXML private TextField noticeTitleField;
    @FXML private TextArea noticeContentArea;
    @FXML private VBox sentNoticeContainer; // Container for notice cards

    // ==========================
    // DATA & SERVICES
    // ==========================
    private Teacher currentTeacher;
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
        // Routine filtering listener
        if (todayOnlyCheckBox != null) {
            todayOnlyCheckBox.setOnAction(event -> loadRoutine(currentTeacher.getFullName()));
        }
    }

    public void initData(Teacher teacher) {
        this.currentTeacher = teacher;
        loadRoutine(teacher.getFullName());
        loadTeacherChatGroups(teacher.getFullName());

        // Load Notice Data
        loadNoticeTargets(teacher.getFullName());
        loadSentNotices(teacher.getFullName());
    }

    // ==========================
    // NAVIGATION METHODS
    // ==========================
    @FXML private void switchTabRoutine() { mainTabPane.getSelectionModel().select(0); }
    @FXML private void switchTabChat() { mainTabPane.getSelectionModel().select(1); }
    @FXML private void switchTabNotice() { mainTabPane.getSelectionModel().select(2); }

    // ==========================
    // NOTICE BOARD LOGIC
    // ==========================

    private void loadNoticeTargets(String teacherName) {
        // Find unique sections where the teacher takes classes
        List<String> mySections = databaseService.getRoutineList().stream()
                .filter(slot -> slot.getTeacherName().equals(teacherName))
                .map(RoutineSlot::getSection)
                .distinct()
                .collect(Collectors.toList());

        noticeTargetSelector.getItems().clear();
        noticeTargetSelector.getItems().add("ALL SECTIONS");
        noticeTargetSelector.getItems().addAll(mySections);
    }

    @FXML
    private void onPostNoticeClick() {
        String target = noticeTargetSelector.getValue();
        String title = noticeTitleField.getText();
        String content = noticeContentArea.getText();

        if (target == null || title.isEmpty() || content.isEmpty()) {
            showAlert("Error", "Please fill all fields.");
            return;
        }

        // Create and save new notice
        Notice notice = new Notice(title, content, target, currentTeacher.getFullName());
        databaseService.addNotice(notice);

        showAlert("Success", "Notice Posted Successfully!");

        // Reset form and reload list
        noticeTitleField.clear();
        noticeContentArea.clear();
        loadSentNotices(currentTeacher.getFullName());
    }

    private void loadSentNotices(String teacherName) {
        sentNoticeContainer.getChildren().clear();

        // Filter notices sent by this teacher
        List<Notice> myNotices = databaseService.getNoticeList().stream()
                .filter(n -> n.getSenderName().equals(teacherName))
                .collect(Collectors.toList());

        if (myNotices.isEmpty()) {
            Label emptyLabel = new Label("You haven't posted any notices yet.");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
            sentNoticeContainer.getChildren().add(emptyLabel);
            return;
        }

        // Generate Notice Cards (Latest first)
        for (int i = myNotices.size() - 1; i >= 0; i--) {
            Notice notice = myNotices.get(i);

            VBox card = new VBox();
            card.getStyleClass().add("notice-card");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label titleLbl = new Label(notice.getTitle());
            titleLbl.getStyleClass().add("notice-title");
            HBox.setHgrow(titleLbl, javafx.scene.layout.Priority.ALWAYS);
            titleLbl.setMaxWidth(Double.MAX_VALUE);

            // Delete Button Implementation
            Button deleteBtn = new Button();
            deleteBtn.setGraphic(new FontIcon("fth-trash-2"));
            deleteBtn.getStyleClass().addAll("button-outlined", "danger");
            deleteBtn.setStyle("-fx-padding: 5 10 5 10;");

            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Notice");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete this notice?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    databaseService.deleteNotice(notice);
                    loadSentNotices(teacherName); // Refresh list
                }
            });

            header.getChildren().addAll(titleLbl, deleteBtn);

            Label targetLbl = new Label("To: " + notice.getTargetSection());
            targetLbl.getStyleClass().add("notice-badge");

            Label contentLbl = new Label(notice.getContent());
            contentLbl.getStyleClass().add("notice-content");
            contentLbl.setWrapText(true);

            Label timeLbl = new Label("🕒 " + notice.getTimestamp());
            timeLbl.getStyleClass().add("notice-footer");

            card.getChildren().addAll(header, targetLbl, new Separator(), contentLbl, new Separator(), timeLbl);
            sentNoticeContainer.getChildren().add(card);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==========================
    // ROUTINE LOGIC (Card View)
    // ==========================

    private void loadRoutine(String teacherFullName) {
        teacherRoutineContainer.getChildren().clear();
        List<RoutineSlot> allSlots = databaseService.getRoutineList();

        // Filter by teacher name
        List<RoutineSlot> filteredSlots = allSlots.stream()
                .filter(slot -> slot.getTeacherName().equals(teacherFullName))
                .collect(Collectors.toList());

        // Filter by Today
        if (todayOnlyCheckBox.isSelected()) {
            String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            filteredSlots = filteredSlots.stream()
                    .filter(slot -> slot.getDay().equalsIgnoreCase(today))
                    .collect(Collectors.toList());
        }

        // Empty state
        if (filteredSlots.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(50));
            Label titleLabel = new Label("No classes found.");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");
            emptyState.getChildren().add(titleLabel);
            teacherRoutineContainer.getChildren().add(emptyState);
            return;
        }

        // Generate Routine Cards
        for (RoutineSlot slot : filteredSlots) {
            VBox card = new VBox(8);
            card.getStyleClass().add("routine-card");

            Label courseCode = new Label(slot.getCourse().getCourseCode());
            courseCode.getStyleClass().add("card-title");

            card.getChildren().addAll(courseCode, new Label(slot.getCourse().getCourseTitle()), new Separator(),
                    new Label("🕒 " + slot.getDay() + " @ " + slot.getTime()),
                    new Label("📍 " + slot.getRoomNo()),
                    new Label("👥 Section: " + slot.getSection()));
            teacherRoutineContainer.getChildren().add(card);
        }
    }

    // ==========================
    // CHAT LOGIC
    // ==========================

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

    private void loadTeacherChatGroups(String teacherName) {
        List<String> myClasses = databaseService.getRoutineList().stream()
                .filter(slot -> slot.getTeacherName().equals(teacherName))
                .map(slot -> slot.getSection() + "_" + slot.getCourse().getCourseCode())
                .distinct()
                .collect(Collectors.toList());
        if (myClasses.isEmpty()) {
            chatCourseSelector.setPromptText("No Assigned Classes");
            return;
        }
        chatCourseSelector.getItems().addAll(myClasses);
        if (!myClasses.isEmpty()) {
            chatCourseSelector.setValue(myClasses.get(0));
            new Thread(() -> connectToChatServer(myClasses.get(0))).start();
        }
        chatCourseSelector.setOnAction(e -> {
            if (chatCourseSelector.getValue() != null)
                new Thread(() -> connectToChatServer(chatCourseSelector.getValue())).start();
        });
    }

    private void connectToChatServer(String uniqueRoomName) {
        try {
            isRunning = false;
            if (socket != null && !socket.isClosed()) socket.close();
            Platform.runLater(() -> {
                chatContainer.getChildren().clear();
                chatContainer.getChildren().add(new Label("Joining " + uniqueRoomName + "..."));
            });
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            isRunning = true;
            writer.println("JOIN:" + uniqueRoomName + ":" + currentTeacher.getFullName() + " (Teacher)");
            String incomingMessage;
            while (isRunning && (incomingMessage = reader.readLine()) != null) {
                String finalMsg = incomingMessage;
                if(finalMsg.contains(":")) {
                    String[] parts = finalMsg.split(":", 2);
                    boolean isMe = parts[0].trim().contains(currentTeacher.getFullName());
                    addMessageToChat(parts[0].trim(), parts[1].trim(), isMe);
                } else {
                    Platform.runLater(() -> chatContainer.getChildren().add(new Label(finalMsg)));
                }
            }
        } catch (IOException e) { if (isRunning) Platform.runLater(() -> chatContainer.getChildren().add(new Label("Connection Lost."))); }
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
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("ClassPilot Login");
        } catch (IOException e) { e.printStackTrace(); }
    }
}