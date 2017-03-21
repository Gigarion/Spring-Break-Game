import Actors.Mob;
import Actors.Player;
import Engine.Engine;
import Util.StdDraw;
import javax.swing.*;
import java.awt.*;

public class GameTest {
     public static void main(String[] args) {
          startIntroGUI();
          startGame();
     }

     private static void startIntroGUI() {
          JFrame frame = new JFrame("Spring Break Game");
          frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

          JLabel emptyLabel = new JLabel("");
          emptyLabel.setPreferredSize(new Dimension(175, 100));
          frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

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
}