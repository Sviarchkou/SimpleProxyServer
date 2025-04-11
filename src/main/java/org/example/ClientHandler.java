package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private final Socket socket;
    InputStream inputStream;
    OutputStream outputStream;
    BufferedReader reader;
    BufferedWriter writer;
    static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        clientHandlers.add(this);
    }

    public void run() {
        while (!socket.isClosed() && socket.isConnected()) {
            try {
                handleClientRequest();
            } catch (IOException e) {
                if (!e.getMessage().equals("Connection reset")){
                    System.out.println("Cannot handle request");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    return;
                }
                try {
                    this.socket.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
                clientHandlers.remove(this);
            }
        }
    }

    private void handleClientRequest() throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.startsWith("CONNECT")){
            socket.close();
            return;
        }

        ArrayList<String> requestStrings = new ArrayList<>();
        requestStrings.add(requestLine);
        String line;
        while (socket.isConnected() && (line = reader.readLine()) != null) {
            requestStrings.add(line);
            if ((line.isEmpty() || line.equals("\r\n")) && !requestLine.startsWith("POST") && !requestLine.startsWith("PATCH") && !requestLine.startsWith("PUT") && !requestLine.startsWith("DELETE"))
                break;
        }
        String host = getHost(requestStrings);
        // String request = String.join("\r\n",(String[])requestStrings.toArray());
        //String line;
        // String host = getHost(request);
        int port = 0;
        try {
            InetAddress.getByName(host);
            // port = Integer.parseInt(getPort(request));
            port = Integer.parseInt(getPort(requestStrings));
        } catch (UnknownHostException e) {
            System.out.println("Host not found");
            return;
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
            return;
        }

        // requestStrings.forEach(System.out::println);
        System.out.println("request: " + requestStrings.getFirst());

        try (Socket serverSocket = new Socket(host, port)) {
            InputStream serverInputStream = serverSocket.getInputStream();
            OutputStream serverOutputStream = serverSocket.getOutputStream();
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(serverInputStream));
            serverOutputStream.write(formatRequest(requestStrings).getBytes(StandardCharsets.UTF_8));

            // serverOutputStream.write(formatRequest(request, host).getBytes(StandardCharsets.UTF_8));
            // serverOutputStream.write(data);
            serverOutputStream.flush();
            byte[] data = serverInputStream.readAllBytes();
            String response = new String(data, StandardCharsets.UTF_8);

            System.out.println("Response from " + host);
            System.out.println(response.split("\r\n")[0]);

            outputStream.write(data);
            outputStream.flush();

        } catch (Exception ex) {
            System.out.println("Something went wrong");
            System.out.println(ex.getMessage());

        }
    }

    private String getHost(String request) {
        for (String line : request.split("\r\n")) {
            if (line.toLowerCase().startsWith("host:")) {
                String hostPort = line.substring(5).trim();
                if (hostPort.contains(":")) {
                    return hostPort.split(":")[0];
                } else {
                    return hostPort;
                }
            }
        }
        return null;
    }

    private String getHost(ArrayList<String> requestStrings) {
        for (String line : requestStrings) {
            if (line.toLowerCase().startsWith("host:")) {
                String hostPort = line.substring(5).trim();
                if (hostPort.contains(":")) {
                    return hostPort.split(":")[0];
                } else {
                    return hostPort;
                }
            }
        }
        return null;
    }

    private String getPort(String request) {
        for (String line : request.split("\r\n")) {
            if (line.toLowerCase().startsWith("host:")) {
                String hostPort = line.substring(5).trim();
                if (hostPort.contains(":")) {
                    return hostPort.split(":")[1];
                } else {
                    return "80"; // По умолчанию HTTP-порт
                }
            }
        }
        return "80";
    }

    private String getPort(ArrayList<String> requestStrings) {
        for (String line : requestStrings) {
            if (line.toLowerCase().startsWith("host:")) {
                String hostPort = line.substring(5).trim();
                if (hostPort.contains(":")) {
                    return hostPort.split(":")[1];
                } else {
                    return "80"; // По умолчанию HTTP-порт
                }
            }
        }
        return "80";
    }


    private String formatRequest(ArrayList<String> requestStrings){
       if (requestStrings.get(2).startsWith("Proxy-Connection")){
            requestStrings.set(2, "Connection: close");
        }
        String[] strs = requestStrings.get(0).split(" ");
        try {
            URL url = new URL(strs[1]);
            requestStrings.set(0, requestStrings.getFirst().replace(url.toString(), url.getFile()));
        } catch (Exception e) { }

        StringBuilder sb = new StringBuilder();
        requestStrings.forEach(str -> sb.append(str).append("\r\n"));
        String request = sb.toString();
        return request;
    }

}
