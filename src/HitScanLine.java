public class HitScanLine extends Actor {
     private double destX;
     private double destY;
     private final int TTL = 2; // frames to be shown
     private int ttl;
     public HitScanLine(double x, double y, double destX, double destY) {
          super((int)x,(int) y, 0);
          this.ttl = TTL;
          this.destX = destX;
          this.destY = destY;
     }

     public void draw() {
          StdDraw.line(x, y, destX, destY);
          ttl--;
     }

     public int getTTL() {
          return ttl;
     }
}