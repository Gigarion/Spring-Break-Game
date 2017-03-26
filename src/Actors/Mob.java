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
//        if (step == 0) {
//            direction = (int) (Math.random() * 4);
//            step = 100;
//        }
//        switch (direction) {
//            case 0:
//                y += offset;
//                break;
//            case 1:
//                x += offset;
//                break;
//            case 2:
//                y -= offset;
//                break;
//            case 3:
//                x -= offset;
//                break;
//            default:
//                break;
//        }
//        step--;
    }

    @Override
    public void draw() {
        StdDraw.filledCircle(x, y, 10);
    }

    public void hit(int damage) {
        hp -= damage;
    }
}