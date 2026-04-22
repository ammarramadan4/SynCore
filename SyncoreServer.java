import java.io.*;
import java.net.*;
import java.util.*;

public class SyncoreServer {
    private static final int PORT = 5000;
    // Thread-safe list to prevent race conditions when broadcasting
    private static List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("Syncore Server is running on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Dire Server Error: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                username = in.readLine();
                String password = in.readLine();

                if (username == null || password == null) return;

                if (!username.matches("^[A-Za-z]+\\d+$")) {
                out.println("Invalid username format.");
                socket.close();
                return;
                }

                if (!password.matches("^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).+$")) {
                out.println("Invalid password format.");
                socket.close();
                return;
                }

                clientWriters.add(out);
                
                // Specific Deliverable: User Joining Updates 
                broadcast("SYSTEM: " + username + " has joined the bulletin board! Say what's up!");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(username + ": " + message);
                }
            } catch (IOException e) {
                System.out.println("User disconnected unexpectedly.");
            } finally {
                if (out != null) {
                    clientWriters.remove(out);
                }
                if (username != null) {
                    // Specific Deliverable: User Leaving Updates 
                    broadcast("SYSTEM: " + username + " has left.");
                }
                try { socket.close(); } catch (IOException e) {}
            }
        }

        private void broadcast(String message) {
            System.out.println(message);
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
