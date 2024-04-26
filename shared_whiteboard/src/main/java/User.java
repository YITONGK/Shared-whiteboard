import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
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
            setUpGUI(userBoard);
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


    public void setUpGUI(IWhiteboard userBoard) {
        gui = new GUI(false);
        gui.setVisible(true);
        gui.setSize(1100, 800);
        setupGUIInteraction(userBoard);
    }

    public void setupGUIInteraction(IWhiteboard userBoard) {
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
            public void updateBackground(byte[] background) {

            }
        });
        try {
            gui.board.shapes = userBoard.getShapes();
            gui.board.shapeColors = userBoard.getShapeColors();
            gui.board.shapeStrokes = userBoard.getShapeStrokes();
            if (userBoard.getBackgroundImage() != null) {
                ByteArrayInputStream bg = new ByteArrayInputStream(userBoard.getBackgroundImage());
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
    public void clearBoard() {
        gui.board.shapes.clear();
        gui.board.shapeColors.clear();
        gui.board.shapeStrokes.clear();
        gui.board.setBackgroundFile((BufferedImage) null);
        gui.board.setBackground(Color.WHITE);
        gui.board.repaint();
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
}
