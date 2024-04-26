import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IWhiteboard extends Remote {
    void addShape(Shape shape, Color color, float stroke) throws RemoteException;
    void addText(String text, int x, int y, Color color, float stroke) throws RemoteException;
    void clearBoard() throws RemoteException;
    void addUser(String username) throws RemoteException;
    void removeUser(String username) throws RemoteException;

    void testConnection() throws RemoteException;
}
