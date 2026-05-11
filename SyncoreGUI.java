import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SyncoreGUI {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 5000;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private String username;

    public static void main(String[] args) {
        new SyncoreGUI().startClient();
    }

    public void startClient() {
        username = JOptionPane.showInputDialog("Enter Username:");
        String password = JOptionPane.showInputDialog("Enter Password:");
        
        // gotta add the regex validation 
        if (!username.matches("^[A-Za-z]+.*\\d+$") || !password.matches("^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).+$")) {
            JOptionPane.showMessageDialog(null, "Invalid credentials format.");
            return;
        }

        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println(username);
            out.println(password);
            
            System.out.println("DEBUG: Connected to server successfully!"); 
            
            setupGUI();
            
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Received: " + message); 
                        chatArea.append(message + "\n");
                    }
                } catch (IOException ex) {
                    System.out.println("Disconnected.");
                }
            }).start();
            
        } catch (IOException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
    }
    
    private void setupGUI() {
        frame = new JFrame("Syncore GUI - " + username);
        frame.setSize(450, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        inputField = new JTextField(25);
        JButton sendBtn = new JButton("Send");
        
        bottomPanel.add(inputField);
        bottomPanel.add(sendBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        
        sendBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg = inputField.getText();
                if (!msg.isEmpty() && out != null) {
                    out.println(msg);
                    inputField.setText("");
                }
            }
        });
        frame.setVisible(true);
    }
}