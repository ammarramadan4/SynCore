import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SyncoreServer {
    private static final int PORT = 5000;
    private static final List<PrintWriter> activeClients = Collections.synchronizedList(new ArrayList<>());
    private static final LinkedList<String> history = new LinkedList<>();
    private static final int MAX_HISTORY = 20;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server connected on port " + PORT);
            while (true) new Thread(new ConnectionHandler(server.accept())).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ConnectionHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ConnectionHandler(Socket s) { this.socket = s; }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                this.out = writer;
                String nameIn = in.readLine();
                // Enforces: Starts with 3+ letters, ends with numbers
                if (nameIn == null || !nameIn.matches("^[A-Za-z]{3,}.*\\d+$")) {
                out.println("ERROR: Username needs 3+ letters followed by any amount of numbers!");
                  return;
                }
                out.println("USER_OK");
                String passIn = in.readLine();
                // Enforces: 1 Capital, 1 Special (!@#$%^&*), 8+ characters total
                if (passIn == null || !passIn.matches("^(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$")) {
                    out.println("ERROR: Password needs 8+ chars, 1 Capital, and 1 Special Char!");
                    return;
                }
                this.username = nameIn;
                out.println("LOGIN_SUCCESS");
                activeClients.add(out);
                synchronized (history) {
                    out.println("--- [System: Last " + history.size() + " Posts] ---");
                    for (String h : history) out.println(h);
                    out.println("--------------------------------");
                }
                broadcast("[SYSTEM]: " + username + " is online.");
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.equalsIgnoreCase("exit")) break;
                    String post = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a")) + "] " + username + ": " + msg;
                    synchronized (history) {
                        if (history.size() >= MAX_HISTORY) history.removeFirst();
                        history.addLast(post);
                    }
                    broadcast(post);
                }
            } catch (IOException e) {
            } finally {
                cleanup();
            }
        }

        private void broadcast(String m) {
            synchronized (activeClients) {
                for (PrintWriter pw : activeClients) pw.println(m);
            }
        }

        private void cleanup() {
            if (out != null) activeClients.remove(out);
            if (username != null) broadcast("[SYSTEM]: " + username + " has signed off.");
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}