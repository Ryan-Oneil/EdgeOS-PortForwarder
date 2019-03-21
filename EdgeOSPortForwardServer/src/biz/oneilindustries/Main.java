package biz.oneilindustries;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {

    private static Settings settings = new Settings();

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(settings.getServerListenPort())) {
            while(true) {
                new Server(serverSocket.accept()).run();
            }
        } catch(IOException e) {
            System.out.println("Server exception " + e.getMessage());
        }
    }
}
