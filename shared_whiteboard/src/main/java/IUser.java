import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IUser extends Remote {
    void addShape(Shape shape, Color color, float stroke) throws RemoteException;
    void addText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException;
    void clearBoard() throws RemoteException;
}
