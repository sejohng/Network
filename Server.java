/*
Shijun Jiang
CSCI 4311 Socket Programming (Assignment 1)
Spring 2024 
Server
*/

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 8989;
    private static Set<String> userNames = new HashSet<>();
    private static Set<PrintWriter> writers = new HashSet<>();
    private static Map<String, ZonedDateTime> userConnectionTimes = new ConcurrentHashMap<>();


    public static void main(String[] args) throws Exception {
        System.out.println("Server is Running...");
        ServerSocket listener = new ServerSocket(PORT);

        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                
                LocalTime timeNow = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss ");
                String timeFormatted = timeNow.format(formatter);
                
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null || name.isEmpty()) {
                        return;
                    }
                    
                    synchronized (userNames) {
                        if (!userNames.contains(name)) {
                            userNames.add(name);
                            userConnectionTimes.put(name, ZonedDateTime.now());
                            break;                         
                        }
                    }
                }
                out.println(timeFormatted + "Welcome " + name); 
                for (PrintWriter writer : writers) {
                    writer.println(" Server: Welcome " + name);
                }
                writers.add(out);                
                
                    while (true) {
                        String message = in.readLine();
                            if (message == null) {
                                break; // This will handle the case of client disconnection
                            }
                            timeNow = LocalTime.now();
                            timeFormatted = timeNow.format(formatter);             
                            if (message.equalsIgnoreCase("Bye")) {
                                synchronized (writers) {
                                    for (PrintWriter writer : writers) {
                                        writer.println(timeFormatted + "Server: Goodbye " + name);
                                    }
                                }
                                return; 
                            } 
                            if (message.equalsIgnoreCase("AllUsers")) {
                                StringBuilder userList = new StringBuilder();
                                userList.append("List of the users connected at ")
                                        .append(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss zzz")))
                                        .append("\n");
                                
                                int count = 1;
                                synchronized (userNames) {
                                    for (String userName : userNames) {
                                        ZonedDateTime connectionTime = userConnectionTimes.get(userName);
                                        userList.append(count++).append(") ").append(userName)
                                                .append(" since ").append(connectionTime.format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy")))
                                                .append("\n");
                                    }
                                }
                                out.println(userList.toString());
                            } else {
                                synchronized (writers) {
                                    for (PrintWriter writer : writers) {
                                        String broadcastMessage = timeFormatted + name + ": " + message;
                                        writer.println(broadcastMessage);
                                    }
                                    System.out.println(timeFormatted + name + ": " + message); 
                                }
                            }                   
                    }

            } catch (IOException e) {
                System.out.println(e);
            } finally {
                
                LocalTime timeNow = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss ");
                String timeFormatted = timeNow.format(formatter);
                
                if (name != null) {
                    userNames.remove(name);
                    System.out.println(timeFormatted + name + " disconnected with a Bye message"); 
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Exception when closing the socket: " + e.getMessage());
                    }            
            }
        }
    }
}