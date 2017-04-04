package Tools;

import Gui.UserBox;
import Maps.GameMap;

import javax.swing.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class SaveMapDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField mapNameField;

    private GameMap gameMap;

    public SaveMapDialog(GameMap gameMap) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.gameMap = gameMap;

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }


    private void onOK() {
        String name = mapNameField.getText();
        if (!name.equals("")) {
            try {
                System.out.println("printing");
                FileOutputStream fos = new FileOutputStream("src/data/maps/" + mapNameField.getText() + ".gm");
                ObjectOutputStream fw = new ObjectOutputStream(fos);
                fw.writeObject(gameMap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        SaveMapDialog dialog = new SaveMapDialog(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
