package Gui;

import Engine.ServerBase;
import Engine.ServerEngine;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by Gig on 4/4/2017.
 */
public class ServerMenu extends JFrame {
    private JButton startServerButton;
    private JPanel contentPane;

    public ServerMenu(int playerCount, int port) {
        port = 3333;
        setContentPane(contentPane);
        setSize(300, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        startServerButton.addActionListener((ActionEvent e) -> {
            ServerBase sb = new ServerBase();
            sb.begin();
            sb.startLobby("Lobby", playerCount);
        });
        setVisible(true);
    }

    public static void main(String[] args) {
        new ServerMenu(15, 3333);
    }
}
