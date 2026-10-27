package server;

import server.server.Server;

import java.util.Scanner;

public class ServerMain {
    public static void main(String[] args) {
        var chessServer = new Server();
        int port = chessServer.run(8080);
        new Scanner(System.in).nextLine();
        chessServer.stop();
    }
}
