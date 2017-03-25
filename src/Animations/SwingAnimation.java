package Animations;

import Actors.Player;
import Util.StdDraw;

import java.io.Serializable;

public class SwingAnimation extends Animation implements Serializable {
     private String imgPath = "img/";
     private String imgLoc;
     protected int startFrame = -1;
     private double srcX, srcY;
     private double deg;
     private double rad;
     public SwingAnimation(Player src, int ttl, String imgName, double destX, double destY) {
          super(src.getX(), src.getY(), ttl);
          this.srcX = src.getX();
          this.srcY = src.getY();
          this.imgLoc = imgPath + imgName;

          this.deg = Math.toDegrees(Math.atan2(destY - src.getY(), destX - src.getX()));
          this.rad = Math.toRadians(deg);
     }
      public void draw(int frame) {
          if (startFrame < 0)
               startFrame = frame;
          double relFrame = frame - startFrame;
          //(8 + Math.cos(rad)) +
          double newX = srcX + (6 * Math.sin(rad)) + ((relFrame / 2) * Math.cos(rad));
          //(10 + Math.sin(rad)) +
          double newY = srcY - (6 * Math.cos(rad)) +  (Math.sin(rad) * relFrame/2);
          StdDraw.picture(newX, newY, imgLoc, 20, 80, deg - 90);
          ttl--;
      }
}