package Gui;

import Actors.Player;
import Engine.ClientEngine;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Gig on 3/27/2017.
 * menu for running the client
 */
public class ClientMenu extends JFrame {
    private JPanel mainMenuPanel;
    private JButton enterGameButton;
    private JTextField playerNameField;
    private JTextField ipTextField;

    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ClientMenu()  {
        super("Main Menu");
        setSize(500, 500);
        setContentPane(mainMenuPanel);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getRootPane().setDefaultButton(enterGameButton);

        setListeners();
        connect();
    }

    private void connect() {
        try {
            socket = new Socket("localhost", 1901);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch(Exception e) {
            System.out.println("bad connect");
        }
    }

    // attempt to log in the user using credentials
    private void login() {
        if (socket == null)
            connect();
        if (playerNameField.getText().equals(""))
            return;
        try {
            // put credentials to server
            outputStream.writeObject(playerNameField.getText());
            outputStream.writeObject(playerNameField.getText());

            // does server say i'm good?
            boolean valid = inputStream.readBoolean();
            if (!valid)
                return;

            // get ip and port of server to link to
            String ip = (String) inputStream.readObject();
            System.out.println(ip);
            int port = inputStream.readInt();
            System.out.println("got port");
            socket.close();
            startGame(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("bad login");
        }
    }

    private void startGame(String ip, int port) {
        ClientEngine ce = new ClientEngine(ip, port);

        String name = playerNameField.getText();
        if (name.equals(""))
            name = "new player";
        ce.setPlayer(new Player(name));
        setVisible(false);
    }

    private void setListeners() {
        enterGameButton.addActionListener((ActionEvent e) -> login());
    }

    public static void main(String[] args) {
        new ClientMenu();
    }
}
