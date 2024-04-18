import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Whiteboard extends JFrame implements ActionListener {


    public Whiteboard() {

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setBounds(100, 100, 880, 520);
//        getContentPane().setLayout(new BorderLayout());
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save as");
        fileMenu.add(newFile);
        fileMenu.add(openFile);
        fileMenu.add(save);
        fileMenu.add(saveAs);
        JMenu manage = new JMenu("Manage");
        menuBar.add(manage);

        JButton rectangle = new JButton(new ImageIcon((this.getClass().getResource("rec.png"))));
        JButton circle = new JButton(new ImageIcon((this.getClass().getResource("circle.png"))));
        JButton oval = new JButton(new ImageIcon((this.getClass().getResource("oval.png"))));
        JButton line = new JButton(new ImageIcon((this.getClass().getResource("line.png"))));
        JButton draw = new JButton(new ImageIcon((this.getClass().getResource("pen.png"))));
        JButton text = new JButton(new ImageIcon((this.getClass().getResource("text.png"))));
        JButton eraser = new JButton(new ImageIcon((this.getClass().getResource("eraser.png"))));

        List<JButton> buttonList = new ArrayList<>();
        buttonList.add(rectangle);
        buttonList.add(circle);
        buttonList.add(oval);
        buttonList.add(line);
        buttonList.add(draw);
        buttonList.add(text);
        buttonList.add(eraser);

        JPanel buttonPane = new JPanel(new FlowLayout());

        Dimension buttonSize = new Dimension(50, 40);
        for (JButton jButton: buttonList) {

            jButton.setPreferredSize(buttonSize);
            buttonPane.add(jButton);
        }

        JLabel sizeLabel = new JLabel("Pen size:");
        sizeLabel.setFont(new Font("Ariel", Font.PLAIN, 20));
        sizeLabel.setSize(buttonSize);

        JComboBox<String> sizeBox = new JComboBox(new String[] {"XS", "S", "M", "L", "XL"});
        sizeBox.setSelectedItem("M");
        sizeBox.setPreferredSize(new Dimension(70, 60));

        buttonPane.add(sizeLabel);
        buttonPane.add(sizeBox);

        JLabel colorLabel = new JLabel("Current Color:");
        colorLabel.setFont(new Font("Ariel", Font.PLAIN, 20));

        JButton colorButton = new JButton("Other Colors");
        colorButton.setPreferredSize(new Dimension(100, 40));
        JLabel colorDisplay = new JLabel();
        colorDisplay.setOpaque(true);
        colorDisplay.setPreferredSize(new Dimension(40, 30));
        colorDisplay.setBackground(Color.BLACK);

        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color chosenColor = JColorChooser.showDialog(null, "Choose a color", colorDisplay.getBackground());
                if (chosenColor != null) {
                    colorDisplay.setBackground(chosenColor);
                }
            }
        });

        buttonPane.add(colorLabel);
        buttonPane.add(colorDisplay);
        buttonPane.add(colorButton);

        JButton userList = new JButton("User list");
        userList.setPreferredSize(new Dimension(100, 40));
        userList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserListWindow userListWindow = new UserListWindow(Whiteboard.this);
                userListWindow.setVisible(true);
            }
        });
        JButton chatBox = new JButton("Chat Box");
        chatBox.setPreferredSize(new Dimension(100, 40));
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChatWindow chatWindow = new ChatWindow(Whiteboard.this);
                chatWindow.setVisible(true);
            }
        });

        buttonPane.add(userList);
        buttonPane.add(chatBox);
        buttonPane.setBounds(22, 6, 1050, 75);
        contentPane.add(buttonPane);

//        getContentPane().add(buttonPane, BorderLayout.NORTH);

        Board board = new Board();
        board.setBounds(25, 85, 1050, 640);
        board.setBackground(Color.white);
//        getContentPane().add(board, BorderLayout.CENTER);
        contentPane.add(board);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }


    public static void main(String[] args) {
        Whiteboard whiteboard = new Whiteboard();
        whiteboard.setVisible(true);
        whiteboard.setSize(1100,800);
    }

    public class UserListWindow extends JDialog {
        private JList<String> userList;
        private DefaultListModel<String> listModel;

        public UserListWindow(Frame owner) {
            super(owner, "User List", true);
            setSize(300, 400);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            listModel = new DefaultListModel<>();
            listModel.addElement("User 1");

            userList = new JList<>(listModel);
            JScrollPane scrollPane = new JScrollPane(userList);
            add(scrollPane, BorderLayout.CENTER);
        }
    }

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

}
