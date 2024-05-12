import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class User extends UnicastRemoteObject implements IUser{

    public void consoleLog(String s) {
        System.out.println(s);
    }
    private IWhiteboard admin;
    private GUI gui;
    private String userId;

    // try to connect to RMI server and join the shared whiteboard
    public User(String ip, int port, String username) throws RemoteException {
        super();
        try {
            Registry registry = LocateRegistry.getRegistry(ip, port);
            admin = (IWhiteboard) registry.lookup("Whiteboard");
            userId = getUserId(username, admin.getUserList());

            if (!admin.requestJoin(userId)) {
                consoleLog("Connection denied by admin.");
                return;
            }

            registry.bind(userId, this);
            setUpGUI(admin, userId);
            admin.addUser(userId);
        } catch (Exception e) {
            throw new RemoteException("Failed to initialize user.", e);
        }
    }

    // get unique user id for registration
    public String getUserId(String username, List<String> currentUserList) {
        if (!currentUserList.contains(username)) {
            return username;
        }
        else {
            for (int i = 1; true; i ++) {
                if (!currentUserList.contains(username + "(" + i + ")")) {
                    return username + "(" + i + ")";
                }
            }
        }
    }


    public static void main(String[] args) throws RemoteException{
        if (args.length == 3) {
            try {
                new User(args[0], Integer.parseInt(args[1]), args[2]);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "The server is currently not available",
                            "Connection Error",
                            JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                });
            }
        }

    }

    // request admin to add the shape drawn by this user
    public void requestAddShape(Shape shape, Color color, float stroke) {
        try {
            admin.addShape(userId, shape, color, stroke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // request admin to add the text typed by this user
    public void requestAddText(String text, int x, int y, Color color, float stroke) {
        try {
            admin.addText(userId, text, x, y, color, stroke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // set up interaction between the user and the canvas
    public void setUpGUI(IWhiteboard admin, String userId) {
        try {
            gui = new GUI(false, admin.getAdminName(), userId, admin);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        gui.setVisible(true);
        gui.setSize(1100, 800);
        setupGUIInteraction(admin);
    }

    // set up event listener in the GUI
    public void setupGUIInteraction(IWhiteboard admin) {
        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    admin.removeUser(userId);
                    admin.broadcastChatMessage(userId + " leaves the whiteboard.\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        gui.board.setDrawingListener(new DrawingListener() {
            @Override
            public void shapeDrawn(Shape shape, Color color, float stroke) {
                requestAddShape(shape, color, stroke);
            }

            @Override
            public void textDrawn(String text, int x, int y, Color color, float stroke) {
                requestAddText(text, x, y, color, stroke);
            }
            @Override
            public void clearBoard() {}

            @Override
            public void updateBackground(byte[] background) {}

        });
        try {
            gui.board.shapes = admin.getShapes();
            gui.board.shapeColors = admin.getShapeColors();
            gui.board.shapeStrokes = admin.getShapeStrokes();
            if (admin.getBackgroundImage() != null) {
                ByteArrayInputStream bg = new ByteArrayInputStream(admin.getBackgroundImage());
                gui.board.setBackgroundFile(ImageIO.read(bg));
            }
            gui.board.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addShape(Shape shape, Color color, float stroke) throws RemoteException {
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(stroke);
        gui.board.repaint();
    }

    @Override
    public void addText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException {
        TextShape shape = new TextShape(text, x, y, stroke);
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(stroke);
        gui.board.repaint();
    }

    @Override
    public void clearBoard() throws RemoteException {
        gui.board.shapes.clear();
        gui.board.shapeColors.clear();
        gui.board.shapeStrokes.clear();
        gui.board.setBackgroundFile(null);
        gui.board.setBackground(Color.WHITE);
        gui.board.setDragStart(null);
        gui.board.setDragEnd(null);
        SwingUtilities.invokeLater(() -> {
            gui.board.repaint();
            gui.board.revalidate();
        });
    }

    @Override
    public void setBackground(byte[] background) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(background);
            gui.board.setBackgroundFile(ImageIO.read(bais));
            gui.board.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateUserList(String adminName, List<String> userList) throws RemoteException {
        gui.userListWindow.updateUserList(adminName, userList);
    }

    @Override
    public void updateChatBox(String message) throws RemoteException {
        gui.chatWindow.updateChatBox(message);
    }

    // show dialog when kicked by the admin
    @Override
    public void exitApplication() throws RemoteException {
        clearBoard();
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "You are kicked out by the admin.",
                    "Kicked Out",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        });
    }

    // show dialog when admin close the server
    @Override
    public void showAdminExit() throws RemoteException {
        clearBoard();
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "The admin shut down the system",
                    "Admin Exits",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        });
    }

    // when first enter the system, chat history will also be displayed
    @Override
    public void loadChatHistory(List<String> messages) throws RemoteException {
        for (String message: messages) {
            gui.chatWindow.updateChatBox(message);
        }
    }
}
