package Actors;

public class Actor {
    protected double x;
    protected double y;
    protected double r;

    public Actor(double x, double y, int r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    // all actors must be Real, be drawable, and have the ability to be shot (lol)
    public void update() {}
    public void draw() {}
    public void hit(int damage) {}

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

    public synchronized double getX() {
        return x;
    }

    public synchronized double getY() {
        return y;
    }

}