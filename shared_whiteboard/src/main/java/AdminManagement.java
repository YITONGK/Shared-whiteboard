import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class AdminManagement extends JDialog {
    private JList<String> userList;
    private DefaultListModel<String> listModel;

    public AdminManagement(Frame owner, String title, boolean modal, IWhiteboard admin) {
        super(owner, title, modal);
        setSize(300, 400);
        setLocationRelativeTo(owner);
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane);

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> removeSelectedUser(admin));
        add(removeButton, BorderLayout.SOUTH);
    }

    // admin can select who to be kicked out
    public void removeSelectedUser(IWhiteboard admin) {
        String selectedUser = userList.getSelectedValue();
        if (selectedUser != null) {
            try {
                admin.kickOutUser(selectedUser);
                admin.broadcastChatMessage(selectedUser + " is kicked out by admin.\n");
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            listModel.removeElement(selectedUser);
        }
    }

    public void addNewUser(String newUser) {
        listModel.addElement(newUser);
    }

}
