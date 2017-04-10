package Animations;

import Actors.Actor;
import Util.StdDraw;

import java.io.Serializable;

/**
 * Created by Gig on 3/27/2017.
 * Shows a stabbing sword animation for the basic melee attack
 */

public class SwingAnimation extends Animation implements Serializable {
    private String imgLoc;
    protected int startFrame = -1;
    private double srcX, srcY;
    private double deg;
    private double rad;
    private Actor src;

    public SwingAnimation(Actor src, int ttl, String imgName, double destX, double destY) {
        super(src.getX(), src.getY(), ttl);
        this.srcX = src.getX();
        this.srcY = src.getY();
        this.imgLoc = "img/" + imgName;
        this.src = null;
        this.deg = Math.toDegrees(Math.atan2(destY - src.getY(), destX - src.getX()));
        this.rad = Math.toRadians(deg);
    }

    public void draw(int frame) {
        if (startFrame < 0)
            startFrame = frame;
        double relFrame = frame - startFrame;
        if (src != null) {
            srcX = src.getX();
            srcY = src.getY();
        }
        double newX = srcX + (6 * Math.sin(rad)) + ((relFrame) * Math.cos(rad));
        //(10 + Math.sin(rad)) +
        double newY = srcY - (6 * Math.cos(rad)) + (Math.sin(rad) * relFrame);
        StdDraw.picture(newX, newY, imgLoc, 20, 80, deg - 90);
        ttl--;
    }

    public void setSrc(Actor src) {
        this.src = src;
    }
}