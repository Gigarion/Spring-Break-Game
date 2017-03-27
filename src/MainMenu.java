import Actors.Player;
import Engine.ClientEngine;
import Engine.ServerEngine;
import Util.StdDraw;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by Gig on 3/27/2017.
 */
public class MainMenu extends JFrame {
    private JTabbedPane tabPanel;
    private JPanel mainMenuPanel;
    private JButton startServerButton;
    private JTextField textField1;
    private JButton enterGameButton;
    private JPanel clientTab;
    private JPanel serverTab;
    private JTextField playerNameField;
    private boolean started;

    public MainMenu() {
        super("Main Menu");
        setSize(500, 500);
        //pack();
        setContentPane(mainMenuPanel);
        setVisible(true);

        setListeners();
    }

    private void startGame() {
        StdDraw.setCanvasSize(1200, 900);
        StdDraw.enableDoubleBuffering();
        StdDraw.setXscale(0, 1200);
        StdDraw.setYscale(0, 900);
        StdDraw.text(600, 450, "Loading");

        ClientEngine ce = new ClientEngine(1000, 1000, 450);

        String name = playerNameField.getText();
        if (name.equals(""))
            name = "new player";
        ce.setPlayer(new Player(name));
        StdDraw.addEngine(ce);
    }

    private void setListeners() {
        startServerButton.addActionListener((ActionEvent e) -> {
            started = true;
            ServerEngine se = new ServerEngine();
        });
        enterGameButton.addActionListener((ActionEvent e) -> {
            startGame();
        });
    }
}
