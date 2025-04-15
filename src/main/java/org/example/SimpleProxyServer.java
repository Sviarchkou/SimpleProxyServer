package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SimpleProxyServer {
    private static final int PORT = 8080;
    private static final int HTTP_PORT = 80;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("127.0.0.2"));
        System.out.println("Proxy server started on port " + PORT);

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                //(new ClientHandler(clientSocket)).run();
                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
