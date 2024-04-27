import java.awt.*;
import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IUser extends Remote {
    void addShape(Shape shape, Color color, float stroke) throws RemoteException;
    void addText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException;
    void clearBoard() throws RemoteException;
    void setBackground(byte[] background) throws RemoteException;
    void updateUserList(String adminName, List<String> userList) throws RemoteException;
    void updateChatBox(String message) throws RemoteException;
    void exitApplication() throws RemoteException;
    void showAdminExit() throws RemoteException;
    void loadChatHistory(List<String> messages) throws RemoteException;

}
