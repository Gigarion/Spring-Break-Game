package Weapons;

import Actors.Actor;
import Projectiles.Projectile;
import Util.StdDraw;

import java.io.Serializable;

/**
 * Created by Gig on 3/28/2017.
 */
public class WeaponRock extends Weapon implements Serializable{
    private int currentClip;
    public WeaponRock() {
        super(50, 10, 100, 100);
        this.isThrowable = true;
    }
    @Override
    public Object fire(Actor src, double destX, double destY) {
        if (fire()) return new Projectile(src, destX, destY, 5, 200, 0.2, 5);
//        {
//            @Override
//            public void draw(boolean selected) {
//                StdDraw.setPenColor(StdDraw.DARK_GRAY);
//                StdDraw.filledCircle(x, y, 5);
//                StdDraw.setPenColor();
//            }
//        };
        return null;
    }

    @Override
    public void addAmmo(int count) {
        currentClip += count;
        if (currentClip > clip)
            currentClip = clip;
    }
}
