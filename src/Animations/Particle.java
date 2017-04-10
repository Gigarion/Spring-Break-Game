package Animations;

import Util.StdDraw;

/**
 * Created by Gig during spring break
 * TODO: make this class work, good small side branch
 */
public class Particle extends Animation {
     public Particle(int x, int y, int ttl) {
          super(x, y, ttl);
     }

     @Override
     public void draw(int frame) {
          StdDraw.setPenColor(StdDraw.BLUE);
          StdDraw.circle(x, y, 2);
          StdDraw.setPenColor();
     }
}