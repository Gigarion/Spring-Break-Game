package Actors;// actors with health, probably mostly used for enemies unless I make the supports killable

import Engine.ActorRequest;
import Maps.MapGrid;
import Util.StdDraw;

import java.util.LinkedList;

public class Mob extends Actor {
    private int hp;

    public Mob(int id, double x, double y, double r, int hp) {
        super(id, x, y, r);
        this.hp = hp;
        this.passesHeight = MapGrid.GROUND_HEIGHT;
    }

    public Mob(ActorStorage as) {
        super(as.id, as.x, as.y, as.r);
        this.hp = (Integer) as.extras.get(ActorStorage.MAXHP);
    }

    public int getHP() {
        return this.hp;
    }

    @Override
    public Iterable<ActorRequest> update() {
        LinkedList<ActorRequest> toReturn = new LinkedList<>();
        toReturn.add(ActorRequest.moveTo(x, y - 0.1));
        return toReturn;
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
    }
}