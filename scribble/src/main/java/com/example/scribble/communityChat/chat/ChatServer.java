package com.example.scribble.communityChat.chat;/*package chat;
import dao.ChatMessageDAO;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<Socket> clientSockets = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcastMessage(message);


                    // Save message to database
                    // Example: Store message in group 1, sender ID 2 (can be dynamic)
                    int groupId = 12; // Replace with dynamic group ID
                    int senderId = 12; // Replace with actual sender's ID from client
                    ChatMessageDAO.addMessage(groupId, senderId, message);
                    //
                     //
                     //
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    clientSockets.remove(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastMessage(String message) {
            for (Socket client : clientSockets) {
                try {
                    PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
                    clientOut.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package chat;

import dao.ChatMessageDAO;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final ConcurrentHashMap<Socket, PrintWriter> clientSockets = new ConcurrentHashMap<>();
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10); // Max 10 threads

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                clientSockets.put(socket, out);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcastMessage(message);

                    // Save message to database
                    int groupId = 12; // Replace with dynamic group ID
                    int senderId = 12; // Replace with actual sender's ID from client
                    ChatMessageDAO.addMessage(groupId, senderId, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter clientOut : clientSockets.values()) {
                clientOut.println(message);
            }
        }

        private void cleanup() {
            try {
                clientSockets.remove(socket);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
} */



import com.example.scribble.communityChat.dao.ChatMessageDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12345; // Port number where the server will listen for incoming connections
    private static final ConcurrentHashMap<Integer, Set<PrintWriter>> groupClients = new ConcurrentHashMap<>(); //maps groupId to a set of PrintWriters (each PrintWriter represents a connected client)
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10); // Max 10 threads

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Try-with-resources ensures the ServerSocket is closed automatically
            System.out.println("Chat Server started on port " + PORT);

            while (true) {  // Keep accepting new clients forever
                Socket clientSocket = serverSocket.accept(); // Accept a new client connection (blocking call)
                threadPool.execute(new ClientHandler(clientSocket)); // Submit a new ClientHandler to handle this client in a separate thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable { //inner class to handle each client
        private Socket socket; // Socket to communicate with the client
        private PrintWriter out; // Output stream to send messages to the client
        private BufferedReader in; // Input stream to receive messages from the client
        private int groupId; // The group ID this client belongs to
        private int senderId; // The sender's user ID

        public ClientHandler(Socket socket) { // Constructor to initialize with the client's socket
            this.socket = socket;
        }

        @Override
        public void run() { // This method will be called when the thread starts
            try {
                // Set up input and output streams for the client
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Get groupId and senderId from the client (could be passed in a handshake)
                String firstMessage = in.readLine(); // First message from client should be "groupId,senderId"
                String[] parts = firstMessage.split(","); // Example: "101,3"
                groupId = Integer.parseInt(parts[0]);
                senderId = Integer.parseInt(parts[1]);

                // Add this client to the group map
                addClientToGroup(groupId, out);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcastMessage(groupId, message); //Send this message to all other clients in the same group

                    // Save message to the database
                    ChatMessageDAO.addMessage(groupId, senderId, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cleanup(); // Clean up (remove the client from the group and close the socket)
            }
        }

        private void addClientToGroup(int groupId, PrintWriter out) { // Adds the current client to the correct group in groupClients
            groupClients.computeIfAbsent(groupId, k -> new HashSet<>()).add(out); // If the group doesn't exist, create a new HashSet
        }

        private void broadcastMessage(int groupId, String message) { // Sends the message to every client in the group
            Set<PrintWriter> clients = groupClients.get(groupId);
            if (clients != null) {
                for (PrintWriter clientOut : clients) {
                    clientOut.println(message); // Send message to all clients in the group
                }
            }
        }

        private void cleanup() {
            try {
                // Remove this client from the group
                Set<PrintWriter> clients = groupClients.get(groupId);
                if (clients != null) {
                    clients.remove(out);
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


