package Animations;

import Projectiles.HitScan;
import Util.StdDraw;

public class HitScanLine extends Animation {
    private double destX;
    private double destY;

    public HitScanLine(HitScan hs) {
        super(hs.getSrcX(), hs.getSrcY(), 2);
        double rads = getRads(hs.getDestX(), hs.getDestY());
        this.destX = x + (hs.getRange() * Math.cos(rads));
        this.destY = y + (hs.getRange() * Math.sin(rads));
    }

    @Override
    public void draw(int frame) {
        StdDraw.line(x, y, destX, destY);
        ttl--;
    }

    private double getRads(double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - y, destX - x));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }
}