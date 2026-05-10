import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SyncoreGUI {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;

    public static void main(String[] args) {
        new SyncoreGUI().startUI();
    }

    public void startUI() {
        frame = new JFrame("Syncore GUI - Testing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        inputField = new JTextField(20);
        JButton sendButton = new JButton("Send");
        
        bottomPanel.add(inputField);
        bottomPanel.add(sendButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (out != null) {
                    out.println(inputField.getText());
                    inputField.setText("");
                }
            }
        });

        frame.setVisible(true);
        connectToServer();
    }

    private void connectToServer() {
        try {
  
            String username = JOptionPane.showInputDialog("Enter Username:");
            String password = JOptionPane.showInputDialog("Enter Password:");

            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(username);
            out.println(password);

            String message;
            while ((message = in.readLine()) != null) {
                chatArea.append(message + "\n");
            }
        } catch (Exception e) {
            chatArea.append("Connection failed.\n");
        }
    }
}