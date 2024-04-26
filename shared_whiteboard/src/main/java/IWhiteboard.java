import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IWhiteboard extends Remote {
    void addShape(String userid, Shape shape, Color color, float stroke) throws RemoteException;
    void addText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException;
    void broadcastShape(String userid, Shape shape, Color color, float stroke) throws RemoteException;
    void broadcastText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException;
    void broadcastClear() throws RemoteException;
    String addUser(String username) throws RemoteException;
    void removeUser(String username) throws RemoteException;

    void testConnection() throws RemoteException;
    List<Shape> getShapes() throws RemoteException;
    List<Color> getShapeColors() throws RemoteException;
    List<Float> getShapeStrokes() throws RemoteException;
}
