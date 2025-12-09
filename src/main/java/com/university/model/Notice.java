package com.university.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notice implements Serializable {
    private String title;
    private String content;
    private String targetSection;
    private String senderName;
    private String timestamp;

    public Notice(String title, String content, String targetSection, String senderName) {
        this.title = title;
        this.content = content;
        this.targetSection = targetSection;
        this.senderName = senderName;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"));
    }

    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTargetSection() { return targetSection; }
    public String getSenderName() { return senderName; }
    public String getTimestamp() { return timestamp; }
}