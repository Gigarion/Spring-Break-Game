package Gui;

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
            new ServerEngine(playerCount, 3333);

        });
        setVisible(true);
    }

    public static void main(String[] args) {
        new ServerMenu(2, 3333);
    }
}
