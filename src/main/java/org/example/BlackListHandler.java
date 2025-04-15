package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BlackListHandler implements AutoCloseable {
    private BufferedReader reader = null;

    public BlackListHandler() {
        try {
            reader = new BufferedReader(new FileReader("blacklist.txt"));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isBlocked(String host) {
        ArrayList<String> blacklist = new ArrayList<>();
        if (reader == null)
            return false;
        try {
            String line = null;
            while((line = reader.readLine()) != null){
                 if (line.equals(host)){
                     return true;
                 }
            }
        } catch (IOException ex) {

        }
        return false;
    }

    public void writeForbiddenMessage(OutputStream outputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("forbiddenPage.html"));
        StringBuilder sb = new StringBuilder();
        while(reader.ready()){
            sb.append(reader.readLine()).append("\r\n");
        }
        String forbiddenResponse = "HTTP/1.1 403 Forbidden\r\n"+
                "Content-Type: text/html\r\n" +
                "Content-Length: " + sb.length() + "\r\n" +
                "\r\n" + sb;
        System.out.println("Proxy: HTTP/1.1 403 Forbidden");
        outputStream.write(forbiddenResponse.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
