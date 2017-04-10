package Gui;

import Actors.Player;
import Engine.ClientEngine;
import Engine.ServerEngine;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;

/**
 * Created by Gig on 3/27/2017.
 * menu for running the client
 */
public class ClientMenu extends JFrame {
    private JPanel mainMenuPanel;
    private JButton enterGameButton;
    private JTextField playerNameField;
    private JTextField ipTextField;


    public ClientMenu()  {
        super("Main Menu");
        setSize(500, 500);
        //pack();
        setContentPane(mainMenuPanel);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getRootPane().setDefaultButton(enterGameButton);

        setListeners();
    }

    private void startGame() {
        ClientEngine ce = new ClientEngine();

        String name = playerNameField.getText();
        if (name.equals(""))
            name = "new player";
        ce.setPlayer(new Player(name));
       //StdDraw.addEngine(ce);
        setVisible(false);
    }

    private void setListeners() {
        enterGameButton.addActionListener((ActionEvent e) -> startGame());
    }

    public static void main(String[] args) {
        new ClientMenu();
    }
}
