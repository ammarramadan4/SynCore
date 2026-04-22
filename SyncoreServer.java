import java.io.*;
import java.net.*;

public class SyncoreServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Syncore Server is running on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("A new client has connected: " + clientSocket.getInetAddress());
            Socket clientSocket2 = serverSocket.accept();
            System.out.println("A new client has connected.");
            
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));
            String username = in.readLine();
            System.out.println(username + " has joined the session.");
            
            String message = in.readLine();
            System.out.println(username + " says: " + message);
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }
} 
