package com.university.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;

    // Stores active clients grouped by room names
    private static Map<String, List<ClientHandler>> groupClients = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("--- ClassPilot Chat Server Started ---");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New Client Connected!");

                // Handle each client in a separate thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;
        private String groupName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // 1. Handle JOIN Request
                // Protocol: "JOIN:GroupName:UserName"
                String request = in.readLine();
                if (request != null && request.startsWith("JOIN:")) {
                    String[] parts = request.split(":");
                    this.groupName = parts[1];
                    this.clientName = parts[2];

                    // Thread-safe addition to the group
                    synchronized (groupClients) {
                        groupClients.computeIfAbsent(groupName, k -> new ArrayList<>()).add(this);
                    }

                    System.out.println(clientName + " joined group: " + groupName);
                    broadcast("Server: " + clientName + " has joined the chat.", true);
                }

                // 2. Message Loop
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("DEBUG: Server received: " + message);

                    if (message.startsWith("MSG:")) {
                        String content = message.substring(4);
                        broadcast(clientName + ": " + content, false);
                    }
                }

            } catch (IOException e) {
                System.out.println(clientName + " disconnected.");
            } finally {
                closeConnection();
            }
        }

        private void broadcast(String msg, boolean isServerMsg) {
            synchronized (groupClients) {
                List<ClientHandler> clients = groupClients.get(groupName);
                if (clients != null) {
                    for (ClientHandler client : clients) {
                        client.out.println(msg);
                    }
                }
            }
        }

        private void closeConnection() {
            try {
                socket.close();
            } catch (IOException e) { e.printStackTrace(); }

            synchronized (groupClients) {
                List<ClientHandler> clients = groupClients.get(groupName);
                if (clients != null) {
                    clients.remove(this);
                    if (clients.isEmpty()) {
                        groupClients.remove(groupName);
                    }
                }
            }
        }
    }
}