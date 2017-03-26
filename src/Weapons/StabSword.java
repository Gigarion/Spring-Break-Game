package Weapons;

import Actors.Actor;
import Projectiles.HitScan;
import Projectiles.Projectile;

/**
 * Created by Gig on 3/25/2017.
 */
public class StabSword extends Weapon {
    private static final int DAMAGE = 100;
    private static final int RELOAD = 100;
    private static final int TYPE = 100;
    private static final int CLIP = 100;
    private static final int FIRERATE = 200;

    public StabSword() {
        super(DAMAGE, RELOAD, TYPE, CLIP, FIRERATE);
    }
    @Override
    public Object fire(Actor src, double destX, double destY) {
        HitScan hs = new HitScan(src, destX, destY, 500, 300, 40);
        hs.setShowLine(true);
        if (fire()) return  hs;
        return null;
    }
}
