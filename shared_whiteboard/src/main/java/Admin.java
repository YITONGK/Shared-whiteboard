import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;


public class Admin extends UnicastRemoteObject implements IWhiteboard {
    public void consoleLog(String s) {
        System.out.println(s);
    }

    public GUI gui;
    private List<String> userList = new ArrayList<>();

    private String adminName;
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
        gui.board.addDrawingListener(new DrawingListener() {
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
        });
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
    public void clearBoard() throws RemoteException {

    }

    @Override
    public String addUser(String username) throws RemoteException {
        if (!userList.contains(username)) {
            userList.add(username);
            return username;
        }
        else {
            for (int i = 1; true; i ++) {
                if (!userList.contains(username + "(" + i + ")")) {
                    userList.add(username + "(" + i + ")");
                    return username + "(" + i + ")";
                }
            }
        }
    }

    @Override
    public void removeUser(String username) throws RemoteException {

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

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        String username = args[1];
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            IWhiteboard adminBoard = new Admin(username, registry);
            registry.bind("Whiteboard", adminBoard);
            System.out.println("Admin starts");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
