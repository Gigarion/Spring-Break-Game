package Actors;// actors with health, probably mostly used for enemies unless I make the supports killable

import Util.StdDraw;

import java.io.Serializable;

public class Mob extends Actor implements Serializable {
    private int hp;
    private double offset = 0.1;
    private int step;
    private int direction = 2;

    public Mob(int id, double x, double y, int r, int hp) {
        super(id, x, y, r);
        this.hp = hp;
        step = 100;
    }

    public int getHP() {
        return this.hp;
    }

    @Override
    public void update() {
        y -= 0.1;
    }
    @Override

    public void draw(boolean selected) {

        if (selected) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.filledSquare(x, y, 10);
            StdDraw.setPenColor();
        }
        StdDraw.filledCircle(x, y, 10);
    }

    public void hit(int damage) {
        hp -= damage;
        System.out.println(hp + " : " + damage);
    }
}