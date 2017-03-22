package Projectiles;

import Actors.Actor;
import Util.StdDraw;

public class Projectile extends Actor {
    private Actor src;
    private double destX, destY;
    private double vel, rad;
    private double range;

    private double startX, startY;

    public Projectile(Actor src, double destX, double destY, int r, double range, double speed) {
        super(src.getX(), src.getY(), r);
        this.src = src;
        this.startX = x;
        this.startY = y;
        this.range = range;
        this.vel = speed;
        this.rad = getAngle(destX, destY);
    }

    public double getDestX() {
        return this.destX;
    }

    public double getDestY() {
        return this.destY;
    }

    public boolean outOfRange() {
        return getDistTraveled() > range;
    }

    public double getDistTraveled() {
        double xDiff = x - startX;
        double yDiff = y - startY;
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    public double getRange() {
        return this.range;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    // thanks stackoverflow
    private double getAngle(double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - y, destX - x));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }

    @Override
    public void update() {
        x += (vel * Math.cos(rad));
        y += (vel * Math.sin(rad));
    }

    public void draw() {
        //StdDraw.picture(x, y, "img/arrow.png", 40, 10, Math.toDegrees(rad));
        StdDraw.filledSquare(x, y, 5);
        System.out.println(vel);
    }
}