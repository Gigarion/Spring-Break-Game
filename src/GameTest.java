import Actors.Mob;
import Actors.Player;
import Engine.Engine;
import Util.StdDraw;

public class GameTest {
     public static void main(String[] args) {
          StdDraw.setCanvasSize(1200, 900);
          StdDraw.enableDoubleBuffering();
          StdDraw.setXscale(0, 1200);
          StdDraw.setYscale(0, 900);
          Engine engine = new Engine(0, 1000, 0, 900);
          engine.setPlayer(new Player());
          engine.addMob(new Mob(300, 900, 10, 50));
          StdDraw.addEngine(engine);
     }

     private void setupScreen() {
          StdDraw.setCanvasSize(1200, 900);
     }
}