import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GUI extends JFrame implements ActionListener {

    public void consoleLog(String s) {
        System.out.println(s);
    }
    private final BasicStroke xs = new BasicStroke(4);
    private final BasicStroke s = new BasicStroke(8);
    private final BasicStroke m = new BasicStroke(12);
    private final BasicStroke l = new BasicStroke(16);
    private final BasicStroke xl = new BasicStroke(20);

    public Board board;




    public GUI(Boolean isAdmin) {


        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        board = new Board();
        board.setBounds(25, 85, 1050, 640);
        board.setBackground(Color.white);
        contentPane.add(board);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        if (isAdmin) {
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);
            JMenuItem newFile = new JMenuItem("New");
            newFile.addActionListener(e -> {
                consoleLog("New in GUI");
                board.clearBoard();
            });
            JMenuItem openFile = new JMenuItem("Open");
            openFile.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select an image file");
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "gif", "bmp"));

                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        board.clearBoard();
                        board.setBackgroundImage(selectedFile);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            JMenuItem save = new JMenuItem("Save");
            save.addActionListener(e -> {
                File defaultFile = new File("default.png");  // Default save location and name
                try {
                    board.saveAsPng(defaultFile);
                    JOptionPane.showMessageDialog(this, "Image saved to " + defaultFile.getAbsolutePath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            JMenuItem saveAs = new JMenuItem("Save as");
            saveAs.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save");
                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try {
                        board.saveAsPng(new File(fileToSave.getAbsolutePath() + ".png"));
                        JOptionPane.showMessageDialog(this, "Image saved to " + fileToSave.getAbsolutePath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            JMenuItem exit = new JMenuItem("Exit");
            exit.addActionListener(e -> {
                int confirmed = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to exit?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmed == JOptionPane.YES_OPTION) {
                    // Perform any cleanup operations you need here
                    dispose(); // Dispose all resources and close the application window
                    System.exit(0); // Ensure JVM is properly closed
                }
            });
            fileMenu.add(newFile);
            fileMenu.add(openFile);
            fileMenu.add(save);
            fileMenu.add(saveAs);
            fileMenu.add(exit);
            JMenu manage = new JMenu("Manage");
            menuBar.add(manage);
        } else {
            JMenu welcome = new JMenu("'s whiteboard");
            menuBar.add(welcome);
        }


        JButton rectangle = new JButton(new ImageIcon((this.getClass().getResource("rec.png"))));
        JButton circle = new JButton(new ImageIcon((this.getClass().getResource("circle.png"))));
        JButton oval = new JButton(new ImageIcon((this.getClass().getResource("oval.png"))));
        JButton line = new JButton(new ImageIcon((this.getClass().getResource("line.png"))));
        JButton draw = new JButton(new ImageIcon((this.getClass().getResource("pen.png"))));
        JButton text = new JButton(new ImageIcon((this.getClass().getResource("text.png"))));
        JButton eraser = new JButton(new ImageIcon((this.getClass().getResource("eraser.png"))));

        rectangle.addActionListener(e -> board.setCurrentMode(Board.Mode.RECTANGLE));
        circle.addActionListener(e -> board.setCurrentMode(Board.Mode.CIRCLE));
        oval.addActionListener(e -> board.setCurrentMode(Board.Mode.OVAL));
        line.addActionListener(e -> board.setCurrentMode(Board.Mode.LINE));
        draw.addActionListener(e -> board.setCurrentMode(Board.Mode.DRAW));
        text.addActionListener(e -> board.setCurrentMode(Board.Mode.TEXT));
        eraser.addActionListener(e -> board.setCurrentMode(Board.Mode.ERASER));

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
        sizeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
                String selectedSize = (String) comboBox.getSelectedItem();
                switch (selectedSize) {
                    case "XS":
                        board.setCurrentStroke(xs); break;
                    case "S":
                        board.setCurrentStroke(s); break;
                    case "M":
                        board.setCurrentStroke(m); break;
                    case "L":
                        board.setCurrentStroke(l); break;
                    case "XL":
                        board.setCurrentStroke(xl); break;
                }
            }
        });

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
                    board.setCurrentColor(chosenColor);
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
                UserListWindow userListWindow = new UserListWindow(GUI.this);
                userListWindow.setVisible(true);
            }
        });
        JButton chatBox = new JButton("Chat Box");
        chatBox.setPreferredSize(new Dimension(100, 40));
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChatWindow chatWindow = new ChatWindow(GUI.this);
                chatWindow.setVisible(true);
            }
        });

        buttonPane.add(userList);
        buttonPane.add(chatBox);
        buttonPane.setBounds(22, 6, 1050, 75);
        contentPane.add(buttonPane);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }


    public static void main(String[] args) {
        GUI gui = new GUI(true);
        gui.setVisible(true);
        gui.setSize(1100,800);
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
