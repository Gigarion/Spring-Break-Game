import Actors.Player;
import Engine.ClientEngine;
import Engine.ServerEngine;
import Util.StdDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class GameGUI implements ActionListener{
    private boolean started;
    private JFrame frame;
    public static void main(String[] args) {
        MainMenu menu = new MainMenu();
        GameGUI gui = new GameGUI();
        //startGame();
    }

    private GameGUI() {

        started = false;
        frame = new JFrame("Spring Break Game");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocation(400, 400);
        frame.setLayout(new BorderLayout());
        frame.setSize(500, 500);

        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(2, 2));

        JTextField enterText = new JTextField(10);
        JButton startButton = new JButton("Start Game");
        JButton serverButton = new JButton("Start Server");
        serverButton.addActionListener(this);
        startButton.addActionListener(this);

        pane.add(enterText);
        pane.add(startButton);
        pane.add(serverButton);
        pane.add(new JLabel(("testing mmore")));

        frame.add(pane, BorderLayout.NORTH);

        frame.pack();
        frame.setVisible(true);

    }

    private static void startGame() {
        StdDraw.setCanvasSize(1200, 900);
        StdDraw.enableDoubleBuffering();
        StdDraw.setXscale(0, 1200);
        StdDraw.setYscale(0, 900);
        ClientEngine ce = new ClientEngine(1000, 1000, 450);
        ce.setPlayer(new Player("player 1"));
        StdDraw.addEngine(ce);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!started) {
            if (e.getActionCommand().equals("Start Game")) {
                started = true;

                startGame();
                frame.setVisible(false);
            }
            if (e.getActionCommand().equals("Start Server")) {
                started = true;
                ServerEngine se = new ServerEngine();
            }

        }
    }
}