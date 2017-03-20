package Animations;

import Actors.Player;
import Util.StdDraw;

public class SwingAnimation extends Animation {
     private String imgPath = "img/";
     private String imgLoc;
     protected int startFrame = -1;
     private Player src;
     private double deg;
     private double rad;
     public SwingAnimation(Player src, int ttl, String imgName, double destX, double destY) {
          super(src.getX(), src.getY(), ttl);
          this.src = src;
          this.imgLoc = imgPath + imgName;

          this.deg = Math.toDegrees(Math.atan2(destY - src.getY(), destX - src.getX()));
          this.rad = Math.toRadians(deg);
     }
      public void draw(int frame) {
          if (startFrame < 0)
               startFrame = frame;
          double relFrame = frame - startFrame;
          //(8 + Math.cos(rad)) +
          double newX = src.getX() + (10 * Math.sin(rad)) + ((relFrame / 2) * Math.cos(rad));
          //(10 + Math.sin(rad)) +
          double newY = src.getY() - (10 * Math.cos(rad)) +  (Math.sin(rad) * relFrame/2);
          StdDraw.picture(newX, newY, imgLoc, 25, 100, deg - 90);
          ttl--;
      }
}