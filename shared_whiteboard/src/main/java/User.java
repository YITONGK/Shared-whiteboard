import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;

public class User {

    public void consoleLog(String s) {
        System.out.println(s);
    }
    private IWhiteboard userBoard;
    private GUI gui;

    public User() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1234);
            userBoard = (IWhiteboard) registry.lookup("Whiteboard");
            consoleLog(userBoard.toString());
            System.out.println("connected to admin");
            userBoard.testConnection();
            gui = new GUI(false);
            gui.setVisible(true);
            gui.setSize(1100, 800);
            setupGUIInteraction();
        } catch (Exception e) {
            consoleLog("connection failed");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        User user = new User();
    }

    public void requestAddShape(Shape shape, Color color, float stroke) {
        try {
            userBoard.addShape(shape, color, stroke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupGUIInteraction() {
        gui.board.addDrawingListener(new DrawingListener() {
            @Override
            public void shapeDrawn(Shape shape, Color color, float stroke) {
                requestAddShape(shape, color, stroke);
            }
        });
    }
}
