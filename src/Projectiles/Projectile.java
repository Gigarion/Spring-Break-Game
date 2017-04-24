package Projectiles;

import Actors.Actor;
import Actors.ActorStorage;
import Engine.ActorRequest;
import Maps.MapGrid;
import Util.StdDraw;

import java.io.Serializable;
import java.util.LinkedList;

public class Projectile extends Actor implements Serializable {
    private int pierceCount;
    private double vel, rad;
    private double range;
    private int damage, srcID;
    private double startX, startY;

    public Projectile(Actor src, double destX, double destY, int r,
                      double range, double speed, int damage, int pierceCount, String image) {
        super(-1, src.getX(), src.getY(), r);
        this.srcID = src.getID();
        this.startX = x;
        this.startY = y;
        this.range = range;
        this.vel = speed;
        this.rad = getAngle(destX, destY);
        this.damage = damage;
        this.image = image;
        this.pierceCount = pierceCount;
        this.canHit = false;
        this.passesHeight = MapGrid.HALF_HEIGHT;
    }

    public Projectile(ActorStorage as) {
        super(as.id, as.x, as.y, as.r);
        this.srcID = (int) as.get(ActorStorage.SRC_ID);
        this.startX = (double) as.get(ActorStorage.START_X);
        this.startY = (double) as.get(ActorStorage.START_Y);
        this.range = (double) as.get(ActorStorage.RANGE);
        this.vel = (double) as.get(ActorStorage.VEL);
        this.rad = (double) as.get(ActorStorage.RAD);
        this.damage = (int) as.get(ActorStorage.DAMAGE);
        this.image = as.image;
        this.pierceCount = (int) as.get(ActorStorage.PIERCE_COUNT);
        this.canHit = false;
        this.passesHeight = 1;
    }

    public boolean outOfRange() {
        return getDistTraveled() > range;
    }

    private double getDistTraveled() {
        double xDiff = x - startX;
        double yDiff = y - startY;
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    public double getStartX() {return this.startX;}
    public double getStartY() {return this.startY;}
    public double getRange() {return this.range;}
    public double getVel() {return this.vel;}
    public double getRad() {return this.rad;}
    public int getDamage() { return damage; }
    public int getPierceCount() {return this.pierceCount;}

    public int getSrcID() {return srcID;}

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

    public int decrementPierceCount() {
        this.pierceCount--;
        return this.pierceCount;
    }
}