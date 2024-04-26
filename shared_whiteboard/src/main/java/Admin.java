import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static java.awt.Font.getFont;

public class Admin extends UnicastRemoteObject implements IWhiteboard {
    public void consoleLog(String s) {
        System.out.println(s);
    }

    public GUI gui;


    public Admin() throws RemoteException {
        super();
        gui = new GUI(true);
        gui.setVisible(true);
        gui.setSize(1100,800);
    }


    @Override
    public void addShape(Shape shape, Color color, float stroke) throws RemoteException {
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(new BasicStroke(stroke));
        gui.board.repaint();
    }

    @Override
    public void addText(String text, int x, int y, Color color, float stroke) {
        int fontSize = (int) stroke * 4;
        Font textFont = new Font("Ariel", Font.PLAIN, 12).deriveFont((float) fontSize);
        TextShape shape = new TextShape(text, x, y, textFont);
        gui.board.shapes.add(shape);
        gui.board.shapeColors.add(color);
        gui.board.shapeStrokes.add(new BasicStroke(stroke));
        gui.board.repaint();
    }

    @Override
    public void clearBoard() throws RemoteException {

    }

    @Override
    public void addUser(String username) throws RemoteException {

    }

    @Override
    public void removeUser(String username) throws RemoteException {

    }

    public void testConnection() {
        consoleLog("hello user");
    }

    public static void main(String[] args) {
        try {
            IWhiteboard adminBoard = new Admin();
            System.out.println(adminBoard.toString());
            Registry registry = LocateRegistry.createRegistry(1234);
            registry.bind("Whiteboard", adminBoard);
            System.out.println("Admin starts");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
