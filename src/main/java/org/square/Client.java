package org.square;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client implements Runnable{

    Socket socket;
    Scanner scanner;
    PrintWriter writeToServer;
    BufferedReader readFromServer;


    public Client() {
        System.out.println("Client started, enter an IPv4 IP Address [default: localhost]");
        scanner = new Scanner(System.in);
        String ip = scanner.nextLine();
        if (ip.isEmpty()) {
            try {
                socket = new Socket("localhost", 1234);
                 writeToServer = new PrintWriter(socket.getOutputStream(), true);
                 readFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                System.out.println("Could not connect to server");
            }
        } else {
            try {
                socket = new Socket(ip, 1234);
                writeToServer = new PrintWriter(socket.getOutputStream(), true);
                readFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (UnknownHostException e) {
                System.out.println("Could not connect to server, is the ip address correct?");
            } catch (IOException e) {
                System.out.println("Could not connect to server, IOException");
            }
    }


    }


    @Override
    public void run() {
        while (true) {
            System.out.println("1 - Create User\n2 - Read All Users\n3 - Read One User\n4 - Update User\n5 - Delete User\n6 - Exit");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1 -> {
                    System.out.println("Enter [UserId, FirstName, LastName, Email, Age, DisplayName], separated by commas.");
                    String[] fields = scanner.nextLine().split(",");
                    if (fields[0].isEmpty()) {
                        fields[0] = "0";
                    }
                    writeToServer.println("CREATE," + fields[0] + "," + fields[1] + "," + fields[2] + "," + fields[3] + "," + fields[4] + "," + fields[5]);
                    readFromServer();
                }
                case 2 -> {
                    writeToServer.println("READALL");
                    readTableFromServer();
                }
                case 3 -> {
                    System.out.println("Enter UserId");
                    int userId = Integer.parseInt(scanner.nextLine());
                    writeToServer.println("READ," + userId);
                    readTableFromServer();
                }
                case 4 -> {
                    System.out.println("Enter [UserId, ColumnName, NewValue], separated by commas.");
                    String[] fields = scanner.nextLine().split(",");
                    writeToServer.println("UPDATE," + fields[0] + "," + fields[1] + "," + fields[2]);
                    readFromServer();
                }
                case 5 -> {
                    System.out.println("Enter UserId");
                    int userId = Integer.parseInt(scanner.nextLine());
                    writeToServer.println("DELETE," + userId);
                    readFromServer();
                }
                case 6 -> System.exit(0);
                default -> System.out.println("Invalid choice");
            }
        }
    }
    public void readFromServer() {
        try {
            String line;
            while (!(line = readFromServer.readLine()).equals("END")) {
                System.out.println(line);
            }
            System.out.println("\nPress enter to continue");
            System.in.read();
        } catch (IOException e) {
            System.out.println("Error reading from server");
            e.printStackTrace();
        }
    }

    public void readTableFromServer() {
        List<String[]> rows = new ArrayList<>();
        String[] headers = {"UserId", "FirstName", "LastName", "Email", "Age", "DisplayName"};
        int[] widths = new int[6];

        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        try {
            String line;
            while (!(line = readFromServer.readLine()).equals("END")) {
                rows.add(line.split(","));
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    widths[i] = Math.max(widths[i], row[i].length());
                }
            }

            StringBuilder border = new StringBuilder();
            for (int width : widths) {
                border.append("+");
                border.append("-".repeat(width + 2));
            }
            border.append("+");
            System.out.println(border);
            StringBuilder headerRow = new StringBuilder("|");
            for (int i = 0; i < headers.length; i++) {
                headerRow.append(String.format(" %-" + widths[i] + "s |", headers[i]));
            }
            System.out.println(headerRow);
            System.out.println(border);

            for (String[] row : rows) {
                StringBuilder rowBuilder = new StringBuilder("|");
                for (int i = 0; i < row.length; i++) {
                    rowBuilder.append(String.format(" %-" + widths[i] + "s |", row[i]));
                }
                System.out.println(rowBuilder);
            }
            System.out.println(border);
        } catch (IOException e) {
            System.out.println("Error reading table from server");
            e.printStackTrace();
        }
    }
}
