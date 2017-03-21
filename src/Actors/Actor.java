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

    public void update() {
    }

    public void draw() {

    }

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