package Animations;

import java.io.Serializable;

public abstract class Animation implements Serializable {
    protected double x;
    protected double y;
    protected int ttl;

    public Animation(double x, double y, int ttl) {
        this.x = x;
        this.y = y;
        this.ttl = ttl;
    }

    public int getTTL() {
        return ttl;
    }

    public abstract void draw(int frame);
}