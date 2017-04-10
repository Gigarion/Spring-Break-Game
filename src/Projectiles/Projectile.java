package Projectiles;

import Actors.Actor;
import Engine.ActorRequest;
import Util.StdDraw;

import java.io.Serializable;
import java.util.LinkedList;

public class Projectile extends Actor implements Serializable {
    private int pierceCount;
    private double vel, rad;
    private double range;
    private int damage;
    private double startX, startY;
    private String image;
    private Actor src;

    public Projectile(Actor src, double destX, double destY, int r,
                      double range, double speed, int damage, int pierceCount, String image) {
        super(-1, src.getX(), src.getY(), r);
        this.startX = x;
        this.startY = y;
        this.range = range;
        this.vel = speed;
        this.rad = getAngle(destX, destY);
        this.damage = damage;
        this.image = image;
        this.src = src;
        this.pierceCount = pierceCount;
        this.canHit = false;
    }

    public boolean outOfRange() {
        return getDistTraveled() > range;
    }

    private double getDistTraveled() {
        double xDiff = x - startX;
        double yDiff = y - startY;
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    public int getDamage() { return damage; }

    // thanks stackoverflow
    private double getAngle(double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - y, destX - x));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }

    @Override
    public Iterable<ActorRequest> update() {
        double newX = x + (vel * Math.cos(rad));
        double newY = y + (vel * Math.sin(rad));
        LinkedList<ActorRequest> toReturn = new LinkedList<>();
        toReturn.add(ActorRequest.moveTo(newX, newY));
        return toReturn;
    }

    public void modifyRange(double percentChange) {
        this.range = range * percentChange;
    }
    public void modifyDamage(double percentChange) {
        this.damage = (int) (damage * percentChange);
    }

    @Override
    public void draw(boolean selected) {
        if (image == null || image.equals("img/-")) {
            try {
                StdDraw.picture(x, y, "src/img/arrow.png", 40, 10, Math.toDegrees(rad));
            } catch (Exception e) {
                StdDraw.picture(x, y, "img/arrow.png", 40, 10, Math.toDegrees(rad));
            }
            StdDraw.circle(x, y, r);
        }
        else {
            try {
                StdDraw.picture(x, y, image, Math.toDegrees(rad));
            } catch(Exception e) {
                StdDraw.filledSquare(x, y , 5);
            }
        }
    }

    @Override
    public void hit(int damage) {}

    public Actor getSrc() {return this.src;}
}