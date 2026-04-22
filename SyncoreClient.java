import java.io.*;
import java.net.*;

public class SyncoreClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            System.out.println("Connected to the Syncore Server!");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = consoleInput.readLine();
            out.println(username);

            System.out.print("Type a message: ");
            String message = consoleInput.readLine();
            out.println(message);
        } catch (IOException e) {
            System.err.println("Connection Error: " + e.getMessage());
        }
    }
}