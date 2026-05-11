import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SyncoreGUI {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static final Color BG_PRIMARY = new Color(30, 30, 35);
    private static final Color BG_SECONDARY = new Color(45, 45, 50);
    private static final Color BTN_COLOR = new Color(90, 100, 250);
    private static final Color NEON_ORANGE = new Color(255, 69, 0);
    private static final Color NEON_BLUE = new Color(0,200, 255);
    
    private JFrame frame;
    private JTextPane chatArea;
    private JTextField inputField;
    private JLabel counterLabel;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String myUser;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SyncoreGUI().launch());
    }

    private void launch() {
        frame = new JFrame("Syncore Messenger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(BG_PRIMARY);
        showLogin();
        frame.setVisible(true);
    }

    private void showLogin() {
        frame.setSize(450, 420);
        frame.setLocationRelativeTo(null);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_PRIMARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = 2; gbc.gridx = 0; gbc.insets = new Insets(10, 40, 10, 40);
        JLabel title = new JLabel("SYNCORE", 0);
        title.setForeground(BTN_COLOR);
        title.setFont(new Font("Times New Roman", 1, 35));
        JTextField userField = createStyledInput("Username", false);
        JPasswordField pField = (JPasswordField) createStyledInput("Password", true);
        JButton loginBtn = new JButton("CONNECT");

        loginBtn.setBackground(BTN_COLOR);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setOpaque(true);
        loginBtn.setBorderPainted(false);
        loginBtn.setFont(new Font("Times New Roman", 1, 14));
        
        ActionListener loginAction = e -> attemptLogin(userField.getText().trim(), new String(pField.getPassword()).trim());
        loginBtn.addActionListener(loginAction);
        userField.addActionListener(loginAction);
        pField.addActionListener(loginAction);

        gbc.gridy = 0; mainPanel.add(title, gbc);
        gbc.gridy = 1; mainPanel.add(userField, gbc);
        gbc.gridy = 2; mainPanel.add(pField, gbc);
        gbc.gridy = 3; mainPanel.add(loginBtn, gbc);
        frame.setContentPane(mainPanel);
        frame.revalidate();
    }

    private void attemptLogin(String u, String p) {
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(u);
            String uResp = in.readLine();
            if (!"USER_OK".equals(uResp)) {
               JOptionPane.showMessageDialog(frame, uResp, "Username Error", 0);
               socket.close(); return;
            }

            out.println(p);
            String pResp = in.readLine();
            if ("LOGIN_SUCCESS".equals(pResp)) {
               this.myUser = u;
             setupChat(u);
            } else { 
             JOptionPane.showMessageDialog(frame, pResp, "Password Error", 0); 
             socket.close(); 
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Server Unreachable", "Error", 0);
        }
    }

    private void setupChat(String user) {
        frame.getContentPane().removeAll();
        frame.setSize(900, 650);
        frame.setLocationRelativeTo(null);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SECONDARY);
        header.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        JLabel userLabel = new JLabel("Logged in as: " + user);
        userLabel.setForeground(Color.LIGHT_GRAY);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        counterLabel = new JLabel(dateStr + " | Messages: 0/20");
        counterLabel.setForeground(BTN_COLOR);
        counterLabel.setFont(new Font("Arial", 1, 13));
        header.add(userLabel, "West");
        header.add(counterLabel, "East");
        
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(BG_PRIMARY);
        chatArea.setFont(new Font("Arial", 0, 15));
        chatArea.setMargin(new Insets(10,10,10,10));
       
        inputField = new JTextField();
        inputField.setBackground(BG_SECONDARY);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.addActionListener(e -> sendMessage());
        
        JButton sendBtn = new JButton("POST");
        sendBtn.setBackground(BTN_COLOR);
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setOpaque(true);
        sendBtn.setBorderPainted(false);
        sendBtn.addActionListener(e -> sendMessage());
        
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(BG_PRIMARY);
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bottom.add(inputField, "Center");
        bottom.add(sendBtn, "East");
        
        frame.setLayout(new BorderLayout());
        frame.add(header, "North");
        frame.add(new JScrollPane(chatArea), "Center");
        frame.add(bottom, "South");
        frame.revalidate();
        frame.repaint();
        inputField.requestFocusInWindow();
        new Thread(this::listen).start();
    }

    private void sendMessage() {
        String m = inputField.getText().trim();
        if (!m.isEmpty() && out != null) {
            out.println(m);
            inputField.setText("");
        }
    }

    private void listen() {
        try {
            String m;
            while ((m = in.readLine()) != null) {
                final String msg = m;
                SwingUtilities.invokeLater(() -> {
                    Color c = NEON_BLUE;
                    if (msg.startsWith("[SYSTEM]") || msg.startsWith("-")) c = Color.WHITE;
                    else if (msg.contains(" " + myUser + ":")) c = NEON_ORANGE;
                    appendToPane(msg + "\n", c);
                    prune();
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                });
            }
        } catch (IOException e) { }
    }

    private void appendToPane(String msg, Color c) {
        StyledDocument doc = chatArea.getStyledDocument();
        Style style = chatArea.addStyle("Style", null);
        StyleConstants.setForeground(style, c);
        try { doc.insertString(doc.getLength(), msg, style); } 
        catch (BadLocationException e) {}
    }

    private void prune() {
        try {
            String text = chatArea.getText();
            int lineCount = 0;
            for (char ch : text.toCharArray()) if (ch == '\n') lineCount++;
            while (lineCount > 20) {
                int firstBreak = text.indexOf('\n');
                if (firstBreak != -1) {
                    chatArea.getStyledDocument().remove(0, firstBreak + 1);
                    text = chatArea.getText();
                    lineCount--;
                } else break;
            }
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            counterLabel.setText(dateStr + " | Messages: " + lineCount + "/20");
        } catch (BadLocationException e) {}
    }

    private JTextField createStyledInput(String label, boolean isSecret) {
        JTextField f = isSecret ? new JPasswordField() : new JTextField();
        f.setBackground(BG_SECONDARY);
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BTN_COLOR), label, 0, 0, null, Color.GRAY));
        return f;
    }
}