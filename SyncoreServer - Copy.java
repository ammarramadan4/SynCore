
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SyncoreServer {
    private static final int PORT = 5000;
    
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());
    private static final LinkedList<String> messageHistory = new LinkedList<>(); 
    private static final int HISTORY_LIMIT = 10;

    public static void main(String[] args) {
        System.out.println("Syncore Server is running on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection established: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Fatal Server Error: " + e.getMessage());
        }
    }

    private static synchronized void addMessageToHistory(String message) {
        messageHistory.add(message);
        if (messageHistory.size() > HISTORY_LIMIT) {
            messageHistory.removeFirst();
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

                if (!username.matches("^[A-Za-z]+.*\\d+$") || !password.matches("^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).+$")) {
                    out.println("Server rejected connection: Invalid credentials format.");
                    socket.close();
                    return;
                }

                clientWriters.add(out);
                
                // Specific Deliverable: Last 10 Posts displayed to newly connected user
                synchronized (messageHistory) {
                    out.println("--- [Last " + messageHistory.size() + " Posts] ---");
                    for (String oldMsg : messageHistory) {
                        out.println(oldMsg);
                    }
                    out.println("-------------------------");
                }

                // Specific Deliverable: User Joining Updates 
                broadcastSystemMessage(username + " has joined the bulletin board!");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break; 
                    }
                    // Specific Deliverable: Timed Post Objects with formatted timestamps that updates in real-time
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a, M/d/yyyy"));
                    String formattedPost = "[" + timestamp + "] " + username + ": " + message;
                    broadcast(formattedPost);
                }
            } catch (IOException e) {
                System.out.println("User disconnected abruptly.");
            } finally {
                if (out != null) {
                    clientWriters.remove(out);
                }
                if (username != null) {
                    broadcastSystemMessage(username + " has left.");
                }
                try { 
                    socket.close(); 
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        private void broadcast(String message) {
            System.out.println("Broadcasting: " + message);
            addMessageToHistory(message);

            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }

        private void broadcastSystemMessage(String msg) {
            String systemMsg = "[SYSTEM]: " + msg;
            System.out.println(systemMsg);
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(systemMsg);
                }
            }
        }
    }
}