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
            consoleLog("Connection failed: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Failed to initialize user.", e);
        }
    }


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
            User user = new User(args[0], Integer.parseInt(args[1]), args[2]);
        }

    }

    public void requestAddShape(Shape shape, Color color, float stroke) {
        try {
            admin.addShape(userId, shape, color, stroke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestAddText(String text, int x, int y, Color color, float stroke) {
        try {
            admin.addText(userId, text, x, y, color, stroke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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


    public void setupGUIInteraction(IWhiteboard admin) {
        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    admin.removeUser(userId);
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
            public void clearBoard() {

            }

            @Override
            public void updateBackground(byte[] background) {

            }

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
        Font textFont = new Font("Ariel", Font.PLAIN, 12).deriveFont(stroke * 4);
        TextShape shape = new TextShape(text, x, y, textFont);
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

    @Override
    public void loadChatHistory(List<String> messages) throws RemoteException {
        for (String message: messages) {
            gui.chatWindow.updateChatBox(message);
        }
    }
}
