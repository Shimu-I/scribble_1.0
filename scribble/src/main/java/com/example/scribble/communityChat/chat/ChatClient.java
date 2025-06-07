/*package chat;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to the Chat Server!");

            // Thread to receive messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("New message: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Sending messages
            String message;
            while ((message = userInput.readLine()) != null) {
                out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package chat;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to the Chat Server!");

            // Thread to receive messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("New message: " + message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            // Sending messages
            String message;
            while ((message = userInput.readLine()) != null) {
                out.println(message);
            }

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
} */

package com.example.scribble.communityChat.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private static final String SERVER_IP = "localhost"; //"localhost" means the server is running on the same computer as the client
    private static final int SERVER_PORT = 12345; //through which the client connects to the server and the server must also be listening to the port

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT); //Tries to connect to the server at localhost:12345.
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)); //reads input from user (keyboard)
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); //sends data to the server. The true means it auto-flushes (sends immediately).
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) { //Receives data from the server.


            System.out.println("Connected to the Chat Server!"); //prints confirmation that the connection to the server was successful.


            //prompts the user to enter a group ID and user ID, reads it from the console, and converts it to an integer
            System.out.print("Enter your Group ID: ");
            int groupId = Integer.parseInt(userInput.readLine());

            System.out.print("Enter your User ID: ");
            int senderId = Integer.parseInt(userInput.readLine());

            // Send the groupId and senderId as the first message (handshake), where the client introduces itself to the server
            out.println(groupId + "," + senderId);

            // Thread to receive messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) { //waits for messages from the server (in.readLine())
                        System.out.println("New message: " + message); //if a message is received, it prints it to the console.
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server."); //if there's an error (like the server disconnecting), it prints a message.
                }
            }).start(); //immediately starts that thread by calling .start()

            // Sending messages
            String message;
            while ((message = userInput.readLine()) != null) { //this loop runs on the main thread and: waits for user input, sends each message to the server using out.println(message)

                out.println(message);
            }

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage()); //catches any IO errors that might occur while trying to connect or communicate with the server
        }
    }
}


