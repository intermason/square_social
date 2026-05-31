package org.square;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    Server server;
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;


    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String command = "";
        try {
            while ((command = reader.readLine()) != null) {
                String[] commandArgs = command.split(",");
                switch (commandArgs[0]) {
                    case "CREATE" -> server.createEntry(Integer.parseInt(commandArgs[1]), commandArgs[2], commandArgs[3], commandArgs[4], Integer.parseInt(commandArgs[5]), commandArgs[6], writer);
                    case "READ" -> server.readOneEntry(Integer.parseInt(commandArgs[1]), writer);
                    case "READALL" -> server.readAllData(writer);
                    case "UPDATE" -> server.updateEntry(Integer.parseInt(commandArgs[1]), commandArgs[2], commandArgs[3], writer);
                    case "DELETE" -> server.deleteEntry(Integer.parseInt(commandArgs[1]), writer);
                    default -> System.out.println("Invalid command");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
