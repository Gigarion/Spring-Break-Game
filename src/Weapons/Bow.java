package Weapons;

import Actors.Actor;
import Projectiles.Projectile;

/**
 * Created by Gig on 3/25/2017.
 */
public class Bow extends Weapon {
    private static final int DAMAGE = 100;
    private static final int RELOAD = 100;
    private static final int TYPE = 100;
    private static final int CLIP = 100;
    private static final int FIRERATE = 200;

    public Bow() {
        super(DAMAGE, RELOAD, TYPE, CLIP, FIRERATE);
    }
    @Override
    public Object fire(Actor src, double destX, double destY) {
        if (fire()) return new Projectile(src, destX, destY, 5, 300, 0.1);
        return null;
    }
}
