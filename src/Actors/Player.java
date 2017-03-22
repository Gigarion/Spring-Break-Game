package Actors;

import Projectiles.HitScan;
import Projectiles.Projectile;
import Util.StdDraw;

public class Player extends Actor {
    private String name;
    private int maxHP;



    private int hp;
    private int level;
    private int exp;

    public Player(String userName) {
        super(300, 300, 10);
        this.name = userName;
        this.maxHP = 100;
        this.hp = maxHP;
    }

    // fully heal player, return result hp
    public void fillHP() {
        this.hp = maxHP;
    }

    // attempt to heal player by healPoints, return result hp
    public void heal(int healPoints) {
        this.hp += healPoints;
        if (this.hp > maxHP)
            this.hp = maxHP;
    }

    // attempt to damage player by damagePoints, return result hp
    public void damage(int damagePoints) {
        this.hp -= damagePoints;
        if (this.hp < 0)
            this.hp = 0;
    }

    // returns either a hitscan or a projectile to register as an attack
    // (or other later???)
    public Object fireWeapon(int which) {
        switch(which) {
            case 0: return new HitScan(this, StdDraw.mouseX(), StdDraw.mouseY(), 200, 1, 80);
            case 1: return new Projectile(this, StdDraw.mouseX(), StdDraw.mouseY(), 5, 200, .1);
            default: return null;
        }
    }

    // shift the player's location by dist in the x direction
    // trusts game engine to call this and protect border cases
    public void moveX(double dist) {
        this.x += dist;
    }
    // shift the player's location by dist in the y direction
    // trusts game engine to call this and protect border cases
    public void moveY(double dist) {
        this.y += dist;
    }
    public void update() {}
    public void draw() {
        StdDraw.filledCircle(x, y, 10);
    }
    public String getName() { return this.name; }
    public int getHP() {return hp;}
    public int getMaxHP() {return maxHP;}
}