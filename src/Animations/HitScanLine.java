package Animations;

import Util.StdDraw;

public class HitScanLine extends Animation {
     private double destX;
     private double destY;
     public HitScanLine(double x, double y, double destX, double destY) {
          super(x,y, 2);
          this.destX = destX;
          this.destY = destY;
     }

     @Override
     public void draw(int frame) {
          StdDraw.line(x, y, destX, destY);
          ttl--;
     }
}