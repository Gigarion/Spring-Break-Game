public class Player extends Actor {
     int maxHP;
     int hp;
     int level;
     int exp;
     public Player() {
          super(300, 300, 10);
          this.maxHP = 100;
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

     public void update() {

     }

     public void draw() {
          StdDraw.filledCircle(x, y, 10);
     }
}