import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserListWindow extends JDialog {
    private JList<String> userList;
    private DefaultListModel<String> listModel;

    public UserListWindow(Frame owner) {
        super(owner, "User List", true);
        setSize(300, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateUserList(String adminName, List<String> users) {
        listModel.clear();
        listModel.addElement(adminName);
        for (String user : users) {
            listModel.addElement(user);
        }
    }

}