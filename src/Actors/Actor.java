package Actors;

import Engine.ActorRequest;
import Mailroom.Package;
import java.io.Serializable;

public abstract class Actor implements Serializable {
    protected double x;
    protected double y;
    protected double r;
    protected int id;
    protected boolean canHit;

    public Actor(int id, double x, double y, double r) {
        this.canHit = true;
        this.id = id;
        this.x = x;
        this.y = y;
        this.r = r;
    }

    // all actors must be Real, be drawable, and have the ability to be shot (lol)
    public abstract Iterable<ActorRequest> update();

    // given an ActorRequest, perform the update.
    // plz override as necessary across the classes
    public void update(ActorRequest ar) {
        if (ar.getType() == ActorRequest.MOVE) {
            double[] coords = Package.extractCoords(ar.getExtra());
            x = coords[0];
            y = coords[1];
        }
    }
    public abstract void draw(boolean selected);
    public abstract void hit(int damage);

    // circular collisions
    public boolean collides(Actor that) {
        double xDiff = this.x - that.x;
        double yDiff = this.y - that.y;
        double distance = Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
        if (distance < that.r || distance < this.r)
            return true;
        return false;
    }

    // simple square hitbox
    public boolean contains(double x, double y) {
        if ((x < this.x + r) && x > (this.x - r) && (y < this.y + r) && (y > this.y - r)) {
            return true;
        }
        return false;
    }

    public double distanceTo(Actor a) {
        return distanceTo(a.getX(), a.getY());
    }

    // distance to the center of the object
    public double distanceTo(double x, double y) {
        double xDiff, yDiff;
        xDiff = this.x - x;
        yDiff = this.y - y;
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    public double getR() {return this.r;}
    public synchronized double getX() {
        return x;
    }
    public synchronized double getY() {
        return y;
    }
    public int getID() { return id; }
    public void setID(int id) { this.id = id; }

    public synchronized void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean isInteractable() {
        return (this instanceof Interactable);
    }

    protected void setCanHit(boolean canHit) { this.canHit = canHit; }
    public boolean canHit() {return canHit;}
}