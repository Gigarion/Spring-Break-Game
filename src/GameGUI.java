import Actors.Mob;
import Actors.Player;
import Engine.Engine;
import Util.StdDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class GameGUI implements ActionListener{
    private boolean started;
    private JFrame frame;
    public static void main(String[] args) {
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
        startButton.addActionListener(this);

        pane.add(enterText);
        pane.add(startButton);
        pane.add(new JLabel("heyo"));
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
        Engine engine = new Engine(10000, 300000, 450);
        engine.setPlayer(new Player());
        engine.addMob(new Mob(300, 900, 10, 50));
        StdDraw.addEngine(engine);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!started) {
            if (e.getActionCommand().equals("Start Game")) {
                started = true;
                startGame();
                frame.setVisible(false);
            }
        }
    }
}