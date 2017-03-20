package Projectiles;
import Actors.Actor;
import Util.StdDraw;

public class Projectile extends Actor {
     private double destX, destY;
     private double vel, rad;
     private double range;
     public Projectile(double x, double y, int r, double range) {
          super(x, y, r);
          this.range = range;
     }

     // x, y, for targets, velocities in pixels/sec

     public void setDest(double x, double y) {
          this.destX = x;
          this.destY = y;
          this.rad = getAngle(x, y);
     }

     // thanks stackoverflow
     private double getAngle(double destX, double destY) {
          double angle =  Math.toDegrees(Math.atan2(destY - y, destX - x));

          if(angle < 0) {
               angle += 360;
          }

          return Math.toRadians(angle);
     }

     public void setSpeed(int vel) {
          this.vel = vel;
     }

     @Override
     public void update() {
          x += (vel * Math.cos(rad));
          super.x = (int) x;
          y += (vel * Math.sin(rad));
          super.y = (int) y;
     }

     public void draw() {
          StdDraw.setPenColor(StdDraw.RED);
          StdDraw.filledSquare(x, y, 5);
          StdDraw.setPenColor(StdDraw.BLACK);
     }
}