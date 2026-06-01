package org.square;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Server or client (S, C)");
        Scanner scanner = new Scanner(System.in);
        char input = scanner.nextLine().charAt(0);
        if (input == 'C') {
            Client client = new Client();
            client.run();
        } else if (input == 'S') {
            Server server = new Server();
        }

    }
}
