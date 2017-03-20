import Actors.Mob;
import Actors.Player;
import Engine.Engine;
import Util.StdDraw;

public class GameTest {
     public static void main(String[] args) {
          StdDraw.setCanvasSize(900, 900);
          StdDraw.enableDoubleBuffering();
          StdDraw.setXscale(0, 900);
          StdDraw.setYscale(0, 900);
          StdDraw.line(0, 1, 1, 0);
          Engine engine = new Engine(0, 900, 0, 900);
          engine.setPlayer(new Player());
          engine.addMob(new Mob(300, 900, 10, 50));
          StdDraw.addEngine(engine);
     }
}