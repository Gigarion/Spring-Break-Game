// particle effects class
public class Particle extends Actor {
     private long ttl; // time to live in milliseconds
     public Particle(int x, int y, int ttl) {
          super(x, y, 0);
          this.ttl = System.currentTimeMillis() + ttl;
     }

     @Override
     public void update() {

     }

     public void draw() {
          StdDraw.setPenColor(StdDraw.BLUE);
          StdDraw.circle(x, y, 2);
          StdDraw.setPenColor();
     }
}