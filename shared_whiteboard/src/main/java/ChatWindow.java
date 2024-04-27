import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatWindow extends JDialog {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public ChatWindow(Frame owner) {
        super(owner, "Chat Window", true);
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inputField.getText().trim().isEmpty()) {
                    chatArea.append(inputField.getText() + "\n");
                    inputField.setText("");
                }
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }
}