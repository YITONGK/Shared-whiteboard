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
    public UserListWindow userListWindow;
    public ChatWindow chatWindow;
    public AdminManagement adminManagement;
    File currentFile = null;

    // build up GUI
    public GUI(Boolean isAdmin, String adminName, String userId, IWhiteboard admin) {

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        // set up drawing area
        board = new Board();
        board.setBounds(25, 85, 1050, 640);
        board.setBackground(Color.white);
        contentPane.add(board);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        // set admin menu
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
                    currentFile = fileChooser.getSelectedFile();
                    try {
                        board.clearBoard();
                        board.setBackgroundImage(currentFile);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            JMenuItem save = new JMenuItem("Save");
            save.addActionListener(e -> {
                File fileToSave;
                if (currentFile != null) {
                    fileToSave = currentFile;
                } else {
                    fileToSave = new File("default.png");
                }
                try {
                    board.saveAsPng(fileToSave);
                    JOptionPane.showMessageDialog(this, "Image saved to " + fileToSave.getAbsolutePath());
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
                    try {
                        admin.broadcastAdminExit();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    // Perform cleanup operations
                    dispose();
                    System.exit(0);
                }
            });
            fileMenu.add(newFile);
            fileMenu.add(openFile);
            fileMenu.add(save);
            fileMenu.add(saveAs);
            fileMenu.add(exit);
            JMenu manageMenu = new JMenu("Manage");
            JMenuItem manageUsers = new JMenuItem("Kick out users");
            manageMenu.add(manageUsers);

            adminManagement = new AdminManagement(GUI.this, "Kick out users", true, admin);
            manageUsers.addActionListener(e -> {
                adminManagement.setVisible(true);
            });

            menuBar.add(manageMenu);
        } else {
            // for regular users, display a message
            JMenu welcome = new JMenu("Hi " + userId + "! Welcome to " + adminName + "'s whiteboard");
            menuBar.add(welcome);
        }
        // load button image
        JButton rectangle = new JButton(new ImageIcon((this.getClass().getResource("rec.png"))));
        JButton circle = new JButton(new ImageIcon((this.getClass().getResource("circle.png"))));
        JButton oval = new JButton(new ImageIcon((this.getClass().getResource("oval.png"))));
        JButton line = new JButton(new ImageIcon((this.getClass().getResource("line.png"))));
        JButton draw = new JButton(new ImageIcon((this.getClass().getResource("pen.png"))));
        JButton text = new JButton(new ImageIcon((this.getClass().getResource("text.png"))));
        JButton eraser = new JButton(new ImageIcon((this.getClass().getResource("eraser.png"))));
        // add button click events
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
        // stroke size configuration
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
        // color configuration
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
        // user list set up
        JButton userList = new JButton("User list");
        userList.setPreferredSize(new Dimension(100, 40));
        userListWindow = new UserListWindow(GUI.this);
        userList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userListWindow.setVisible(true);
            }
        });
        // chat window set up
        JButton chatBox = new JButton("Chat Box");
        chatBox.setPreferredSize(new Dimension(100, 40));
        chatWindow = new ChatWindow(GUI.this, userId, admin);
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

}
