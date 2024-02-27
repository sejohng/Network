/*
Shijun Jiang
CSCI 4311 Socket Programming (Assignment 1)
Spring 2024 
Client
*/

import java.io.*;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Client {
    private static volatile boolean running = true;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Client <host name> <port number>"); //java Client localhost 8989
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        long startTime = System.currentTimeMillis();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            long endTime = System.currentTimeMillis();
            long totalTimeMillis = endTime - startTime;
            String totalTime = String.format("%d:%02d.%04d",
                                            totalTimeMillis / 60000,
                                            (totalTimeMillis / 1000) % 60,
                                            totalTimeMillis % 1000);

            String finishedAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy"));

            long memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);

            System.out.println("-------------------------------------------------------");
            System.out.println("Total time: " + totalTime + "s");
            System.out.println("Finished at: " + finishedAt);
            System.out.println("Final Memory: " + memory + "M/ " + totalMemory + "M");
            System.out.println("-------------------------------------------------------");
        }));

        try (
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            new Thread(() -> {
                try {
                    String fromServer;
                    while (running && (fromServer = in.readLine()) != null) {
                        if (fromServer.equals("SUBMITNAME")) {
                            System.out.print("Enter your username: ");
                        } else if (fromServer.startsWith("NAMEACCEPTED")) {
                            System.out.println("Welcome " + fromServer.substring(13));
                        } else {
                            System.out.println(fromServer);
                        }
                    }
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }).start();

            while (running) {
                String userMessage = stdIn.readLine();
                if (userMessage != null) {
                    out.println(userMessage);
                    if ("bye".equalsIgnoreCase(userMessage.trim())) {
                        System.out.println("Disconnecting...");
                        running = false;
                    }
                }
            }

            // Allow the reader thread to finish its execution
            Thread.sleep(1000);
        } catch (IOException ex) {
            System.err.println("IOException: " + ex.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
