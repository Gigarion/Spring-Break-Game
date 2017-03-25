package Projectiles;

import Actors.Actor;

import java.io.Serializable;

/**
 * Created by Gig on 3/20/2017.
 * pojo describing a hitscan for communication purposes and such
 */
public class HitScan implements Serializable {
    private int damage, pierceCount;
    private double srcX, srcY, destX, destY, range;
    private boolean showLine;
    public HitScan(Actor src, double destX, double destY, int damage, int pierceCount, double range) {
        this.srcX = src.getX();
        this.srcY = src.getY();
        this.destX = destX;
        this.destY = destY;
        this.damage = damage;
        this.pierceCount = pierceCount;
        this.range = range;
        this.showLine = true;
    }

    public void setShowLine(boolean showLine) {
        this.showLine = showLine;
    }

    public boolean getShowLine() {
        return showLine;
    }

    //public Actor getSrc() {
        //return this.src;
    //}

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

    public double getRange() {
        return this.range;
    }
}
