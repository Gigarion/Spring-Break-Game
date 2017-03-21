package Projectiles;

import Actors.Actor;

/**
 * Created by Gig on 3/20/2017.
 * pojo describing a hitscan for communication purposes and such
 */
public class HitScan {
    private Actor src;
    private int damage, pierceCount;
    private double srcX, srcY, destX, destY;
    public HitScan(Actor src, double destX, double destY, int damage, int pierceCount) {
        this.src = src;
        this.srcX = src.getX();
        this.srcY = src.getY();
        this.destX = destX;
        this.destY = destY;
        this.damage = damage;
        this.pierceCount = pierceCount;
    }

    public int getDamage() {
        return damage;
    }

    public int getPierceCount() {
        return pierceCount;
    }

    public double getSrcX() {
        return srcX;
    }

    public double getSrcY() {
        return srcY;
    }

    public double getDestX() {
        return destX;
    }

    public double getDestY() {
        return destY;
    }
}
