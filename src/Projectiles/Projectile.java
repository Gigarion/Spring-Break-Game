package Projectiles;

import Actors.Actor;
import Util.StdDraw;

public class Projectile extends Actor {
    private double destX, destY;
    private double vel, rad;
    private double range;
    private double startX, startY;

    public Projectile(double x, double y, int r, double range) {
        super(x, y, r);
        this.startX = x;
        this.startY = y;
        this.range = range;
    }

    // x, y, for targets, velocities in pixels/sec

    public void setDest(double x, double y) {
        this.destX = x;
        this.destY = y;
        this.rad = getAngle(x, y);
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

    // thanks stackoverflow
    private double getAngle(double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - y, destX - x));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }

    public void setSpeed(double vel) {
        this.vel = vel;
    }

    @Override
    public void update() {
        x += (vel * Math.cos(rad));
        y += (vel * Math.sin(rad));
    }

    public void draw() {
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.filledSquare(x, y, 5);
        StdDraw.setPenColor(StdDraw.BLACK);
    }
}