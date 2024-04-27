import javax.swing.*;
import java.awt.*;

public class UserListWindow extends JDialog {
    private JList<String> userList;
    private DefaultListModel<String> listModel;

    public UserListWindow(Frame owner) {
        super(owner, "User List", true);
        setSize(300, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        listModel.addElement("User 1");

        userList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane, BorderLayout.CENTER);
    }
}