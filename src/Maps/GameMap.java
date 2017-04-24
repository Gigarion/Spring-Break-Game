package Maps;

import Actors.Actor;
import Actors.ActorStorage;
import Util.StdDraw;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.LinkedList;

/**
 * Created by Gig on 4/3/2017.
 * GameMap contains the image url, the mapgrid, logical boundaries, and
 * associated actors with a given map instance.  useful for drawing maps you know
 */
public class GameMap {
    private MapGrid mapGrid;
    private int maxX, maxY;
    private String image;
    private LinkedList<Actor> actors;

    public GameMap(int maxX, int maxY, String image) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.image = image;
        this.actors = new LinkedList<>();
    }

    public GameMap(String filename) {
        if (!filename.contains(".gm"))
            filename += ".gm";
        try {
            System.out.println("loading");
            FileInputStream fis = new FileInputStream("data/Maps/" + filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            GameMapStorage gm = (GameMapStorage) ois.readObject();

            this.mapGrid = gm.mapGrid;
            this.maxX = gm.maxX;
            this.maxY = gm.maxY;
            this.image = gm.image;
            this.actors = new LinkedList<>();
            for (ActorStorage as : gm.actors) {
                this.actors.add(ActorStorage.getActor(as));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMaxX() {return this.maxX;}
    public int getMaxY() {return this.maxY;}

    public void setMapGrid(MapGrid mapGrid) {
        this.mapGrid = mapGrid;
    }

    public MapGrid getMapGrid() {
        return mapGrid;
    }

    public LinkedList<Actor> getActors() {
        return new LinkedList<>(actors);
    }

    public void addActor(Actor a) {
        actors.add(a);
    }

    String getImage() { return this.image; }

    public GameMapStorage getStorage() {
        return new GameMapStorage(this);
    }

    public void draw() {
        try {
            StdDraw.picture(maxX / 2, maxY / 2, "src/img/Maps/" + image, maxX, maxY);
        } catch (Exception e) {
            StdDraw.picture(maxX / 2, maxY / 2, "img/Maps/" + image, maxX, maxY);
        }
    }
}
