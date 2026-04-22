import java.io.*;
import java.net.*;

public class SyncoreClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Connecting to SynCore Server...");

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            String username;
            while (true) {
                System.out.print("Enter your username (Letters followed by numbers, e.g., Ammar123): ");
                username = consoleInput.readLine().trim();
                
                // Regex: Starts with letters, ends with at least one number
                if (username.matches("^[A-Za-z]+.*\\d+$")) {
                    break; 
                }
                System.out.println("Invalid! Username must have letters followed by at least one number.");
            }
            out.println(username);

            String password;
            while (true) {
                System.out.print("Enter your password (1 Capital, 1 Special Character): ");
                password = consoleInput.readLine().trim();
                
                // Regex: Lookahead for 1 Capital, Lookahead for 1 Special Char
                if (password.matches()) {
                    break; 
                }
                System.out.println("Invalid! Password must contain at least one capital letter and one special character.");
            }
            out.println(password); 

            Thread listener = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("\n" + message);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println("\nDisconnected from server.");
                }
            });
            listener.start();

            System.out.println("Type messages (type 'exit' to quit):");
            System.out.print("> ");

            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }
                out.println(userInput);
                System.out.print("> ");
            }

        } catch (IOException e) {
            System.err.println("Connection Error: " + e.getMessage());
        }

        System.out.println("Disconnected successfully from SynCore.");
    }
}
