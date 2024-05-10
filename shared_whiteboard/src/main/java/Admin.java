import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class Admin extends UnicastRemoteObject implements IWhiteboard {
    public void consoleLog(String s) {
        System.out.println(s);
    }

    public GUI gui;
    private List<String> userList = new ArrayList<>();
    private List<String> messages = new ArrayList<>();
    private final String adminName;
    private Registry registry;

    public Admin(String adminName, Registry registry) throws RemoteException {
        super();
        this.adminName = adminName;
        this.registry = registry;
        gui = new GUI(true, adminName, adminName, this);
        gui.setVisible(true);
        gui.setSize(1100,800);
        setupGUIInteraction();
    }

    public static void main(String[] args) {
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String adminName = args[2];
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            IWhiteboard adminBoard = new Admin(adminName, registry);
            adminBoard.updateUserListWindow(adminName, new ArrayList<>());
            registry.bind("Whiteboard", adminBoard);
            System.out.println("Admin starts");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GUI components and interaction setup
    public void setupGUIInteraction() {
        // inform clients upon server termination
        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    broadcastAdminExit();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // broadcast modifications on shared whiteboard
        gui.board.setDrawingListener(new DrawingListener() {
            @Override
            public void shapeDrawn(Shape shape, Color color, float stroke) {
                try {
                    broadcastShape("admin" ,shape, color, stroke);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void textDrawn(String text, int x, int y, Color color, float stroke) {
                try {
                    broadcastText("admin", text, x, y, color, stroke);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void clearBoard() {
                try {
                    broadcastClear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void updateBackground(byte[] background) {
                try {
                    broadcastBackground(background);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Process user join request with admin confirmation
    public boolean requestJoin(String userId) throws RemoteException {
        // use FutureTask to enforce admin process this request ASAP
        FutureTask<Boolean> futureTask = new FutureTask<>(() -> {
            int result = JOptionPane.showConfirmDialog(gui,
                    "Do you want to allow " + userId + " to join?",
                    "User Join Request",
                    JOptionPane.YES_NO_OPTION);
            return result == JOptionPane.YES_OPTION;
        });
        try {
            SwingUtilities.invokeAndWait(futureTask);
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RemoteException("Failed to process join request.", e);
        }
        try {
            return futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // Add shapes (including line, rectangle, circle, oval, free draw, eraser) to local and remote canvas
    @Override
    public synchronized void addShape(String userId, Shape shape, Color color, float stroke) throws RemoteException {
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(stroke);
        gui.board.repaint();
        broadcastShape(userId, shape, color, stroke);
    }

    // Add text to local and remote canvas
    @Override
    public synchronized void addText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException {
        TextShape shape = new TextShape(text, x, y, stroke);
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(stroke);
        gui.board.repaint();
        broadcastText(userId, text, x, y, color, stroke);
    }

    // Broadcast a shape to all connected users except the sender
    @Override
    public synchronized void broadcastShape(String userId, Shape shape, Color color, float stroke) throws RemoteException {
        try {
            for (String user: userList) {
                if (!user.equals(userId)) {
                    IUser u = (IUser) registry.lookup(user);
                    u.addShape(shape, color, stroke);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcast text to all connected users except the sender
    @Override
    public synchronized void broadcastText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException {
        try {
            for (String user: userList) {
                if (!user.equals(userId)) {
                    IUser u = (IUser) registry.lookup(user);
                    u.addText(userId, text, x, y, color, stroke);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcast command to clear the board to all users
    @Override
    public synchronized void broadcastClear() {
        try {
            for (String user: userList) {
                IUser u = (IUser) registry.lookup(user);
                u.clearBoard();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcast a new background image to all users
    @Override
    public synchronized void broadcastBackground(byte[] background) throws RemoteException {
        try {
            for (String user: userList) {
                IUser u = (IUser) registry.lookup(user);
                u.setBackground(background);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcast updated user list to all users
    @Override
    public synchronized void broadcastUserList(String adminName, List<String> userList) throws RemoteException {
        try {
            for (String user: userList) {
                IUser u = (IUser) registry.lookup(user);
                u.updateUserList(adminName, userList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcast a chat message to all users
    @Override
    public synchronized void broadcastChatMessage(String message) throws RemoteException {
        messages.add(message);
        gui.chatWindow.updateChatBox(message);
        try {
            for (String user: userList) {
                IUser u = (IUser) registry.lookup(user);
                u.updateChatBox(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcast admin exit notification to all users, and let them exit as well
    @Override
    public synchronized void broadcastAdminExit() throws RemoteException {
        try {
            for (String user: userList) {
                IUser u = (IUser) registry.lookup(user);
                u.showAdminExit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add a new user to the system
    @Override
    public synchronized void addUser(String username) throws RemoteException {
        userList.add(username);
        updateUserListWindow(adminName, userList);
        updateUserOnlyList(username);
        loadHistoryMessage(username, messages);
    }

    // Remove a user from the system
    @Override
    public synchronized void removeUser(String userId) throws RemoteException {
        userList.remove(userId);
        updateUserListWindow(adminName, userList);
        try {
            registry.unbind(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Manager kick out a user
    @Override
    public synchronized void kickOutUser(String userId) throws RemoteException {
        userList.remove(userId);
        updateUserListWindow(adminName, userList);
        try {
            IUser userKicked = (IUser) registry.lookup(userId);
            userKicked.exitApplication();
            registry.unbind(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update the user list display in GUI
    @Override
    public synchronized void updateUserListWindow(String adminName, List<String> userList) throws RemoteException {
        gui.userListWindow.updateUserList(adminName, userList);
        broadcastUserList(adminName, userList);
    }

    // Update the admin management when a new user joins
    @Override
    public void updateUserOnlyList(String newUser) throws RemoteException {
        gui.adminManagement.addNewUser(newUser);
    }

    // Load and send historical chat messages to a new user
    @Override
    public void loadHistoryMessage(String userId, List<String> messages) throws RemoteException {
        try {
            IUser newUser = (IUser) registry.lookup(userId);
            newUser.loadChatHistory(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Retrieve the current list of users
    @Override
    public synchronized List<String> getUserList() {
        return userList;
    }

    // Retrieve the administrator's name
    @Override
    public synchronized String getAdminName() {
        return adminName;
    }

    // Retrieve the current list of shapes on the board
    @Override
    public synchronized List<Shape> getShapes() throws RemoteException {
        return gui.board.shapes;
    }

    // Retrieve the current list of colors for shapes on the board
    @Override
    public synchronized List<Color> getShapeColors() throws RemoteException {
        return gui.board.shapeColors;
    }

    // Retrieve the current list of strokes for shapes on the board
    @Override
    public synchronized List<Float> getShapeStrokes() throws RemoteException {
        return gui.board.shapeStrokes;
    }

    // Retrieve the current background image as a byte array
    @Override
    public synchronized byte[] getBackgroundImage() throws RemoteException {
        try {
            if (gui.board.getBackgroundFile() != null) {
                return gui.board.serializeImage(gui.board.getBackgroundFile());
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
