// actors with health, probably mostly used for enemies unless I make the supports killable

public class Mob extends Actor {
     int hp;
     double offset = 0.35;
     public Mob(int x, int y, int r, int hp) {
          super(x, y, r);
          this.hp = hp;
     }

     public int getHP() {
          return this.hp;
     }

     @Override
     public void update() {
          // x += offset;
          y -= offset;
     }

     @Override
     public void draw() {
          StdDraw.filledCircle(x, y, 10);
     }

     public void hit(int damage) {
          hp -= damage;
     }
}