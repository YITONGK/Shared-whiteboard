import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IWhiteboard extends Remote {
    boolean requestJoin(String userId) throws RemoteException;
    void addShape(String userid, Shape shape, Color color, float stroke) throws RemoteException;
    void addText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException;
    void broadcastShape(String userid, Shape shape, Color color, float stroke) throws RemoteException;
    void broadcastText(String userId, String text, int x, int y, Color color, float stroke) throws RemoteException;
    void broadcastClear() throws RemoteException;
    void broadcastBackground(byte[] background) throws RemoteException;
    void broadcastUserList(String adminName, List<String> userList) throws RemoteException;
    void broadcastChatMessage(String message) throws RemoteException;
    void broadcastAdminExit() throws RemoteException;
    void addUser(String username) throws RemoteException;
    void removeUser(String username) throws RemoteException;
    void kickOutUser(String userId) throws RemoteException;
    void updateUserListWindow(String adminName, List<String> userList) throws RemoteException;
    void updateUserOnlyList(String newUser) throws RemoteException;
    void loadHistoryMessage(String username, List<String> messages) throws RemoteException;
    List<String> getUserList() throws RemoteException;
    String getAdminName() throws RemoteException;
    List<Shape> getShapes() throws RemoteException;
    List<Color> getShapeColors() throws RemoteException;
    List<Float> getShapeStrokes() throws RemoteException;
    byte[] getBackgroundImage() throws RemoteException;
}
