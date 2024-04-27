import javax.swing.*;
import java.awt.*;
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

    private final String adminName;
    private Registry registry;

    public Admin(String adminName, Registry registry) throws RemoteException {
        super();
        this.adminName = adminName;
        this.registry = registry;
        gui = new GUI(true);
        gui.setVisible(true);
        gui.setSize(1100,800);
        setupGUIInteraction();
    }

    public void setupGUIInteraction() {
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

    public boolean requestJoin(String userId) throws RemoteException {
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

    @Override
    public void addShape(String userId, Shape shape, Color color, float stroke) throws RemoteException {
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(stroke);
        gui.board.repaint();
        broadcastShape(userId, shape, color, stroke);
    }

    @Override
    public void addText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException {
        int fontSize = (int) stroke * 4;
        Font textFont = new Font("Ariel", Font.PLAIN, 12).deriveFont((float) fontSize);
        TextShape shape = new TextShape(text, x, y, textFont);
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(stroke);
        gui.board.repaint();
        broadcastText(userId, text, x, y, color, stroke);
    }

    @Override
    public void broadcastShape(String userId, Shape shape, Color color, float stroke) throws RemoteException {
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

    @Override
    public void broadcastText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException {
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

    @Override
    public void broadcastClear() {
        try {
            for (String user: userList) {
                IUser u = (IUser) registry.lookup(user);
                u.clearBoard();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void broadcastBackground(byte[] background) throws RemoteException {
        try {
            for (String user: userList) {
                IUser u = (IUser) registry.lookup(user);
                u.setBackground(background);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void broadcastUserList(String adminName, List<String> userList) throws RemoteException {
        try {
            for (String user: userList) {
                IUser u = (IUser) registry.lookup(user);
                u.updateUserList(adminName, userList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addUser(String username) throws RemoteException {
        userList.add(username);
        updateUserListWindow(adminName, userList);
    }

    @Override
    public void removeUser(String userId) throws RemoteException {
        userList.remove(userId);
        updateUserListWindow(adminName, userList);
        try {
            registry.unbind(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUserListWindow(String adminName, List<String> userList) throws RemoteException {
        gui.userListWindow.updateUserList(adminName, userList);
        broadcastUserList(adminName, userList);
    }

    @Override
    public List<String> getUserList() {
        return userList;
    }

    @Override
    public void testConnection() {
        consoleLog("hello user");
    }

    @Override
    public List<Shape> getShapes() throws RemoteException {
        return gui.board.shapes;
    }

    @Override
    public List<Color> getShapeColors() throws RemoteException {
        return gui.board.shapeColors;
    }

    @Override
    public List<Float> getShapeStrokes() throws RemoteException {
        return gui.board.shapeStrokes;
    }

    @Override
    public byte[] getBackgroundImage() throws RemoteException {
        try {
            if (gui.board.getBackgroundFile() != null) {
                return gui.board.serializeImage(gui.board.getBackgroundFile());
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        String adminName = args[1];
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
}
