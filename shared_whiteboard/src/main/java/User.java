import java.awt.*;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class User extends UnicastRemoteObject implements IUser{

    public void consoleLog(String s) {
        System.out.println(s);
    }
    private IWhiteboard userBoard;
    private GUI gui;
    private String userId;

    public User(String ip, int port, String username) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(ip, port);
            userBoard = (IWhiteboard) registry.lookup("Whiteboard");
            userId = userBoard.addUser(username);
            registry.bind(userId, this);
            setUpGUI();
        } catch (Exception e) {
            consoleLog("connection failed");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException{
        if (args.length == 3) {
            User user = new User(args[0], Integer.parseInt(args[1]), args[2]);
        }

    }

    public void requestAddShape(Shape shape, Color color, float stroke) {
        try {
            userBoard.addShape(userId, shape, color, stroke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestAddText(String text, int x, int y, Color color, float stroke) {
        try {
            userBoard.addText(userId, text, x, y, color, stroke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setUpGUI() {
        gui = new GUI(false);
        gui.setVisible(true);
        gui.setSize(1100, 800);
        setupGUIInteraction();
    }

    public void setupGUIInteraction() {
        gui.board.addDrawingListener(new DrawingListener() {
            @Override
            public void shapeDrawn(Shape shape, Color color, float stroke) {
                requestAddShape(shape, color, stroke);
            }

            @Override
            public void textDrawn(String text, int x, int y, Color color, float stroke) {
                requestAddText(text, x, y, color, stroke);
            }
        });
    }

    @Override
    public void addShape(Shape shape, Color color, float stroke) throws RemoteException {
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(new BasicStroke(stroke));
        gui.board.repaint();
    }

    @Override
    public void addText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException {
        int fontSize = (int) stroke * 4;
        Font textFont = new Font("Ariel", Font.PLAIN, 12).deriveFont((float) fontSize);
        TextShape shape = new TextShape(text, x, y, textFont);
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(new BasicStroke(stroke));
        gui.board.repaint();
    }
}
