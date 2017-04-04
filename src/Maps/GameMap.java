package Maps;

import Actors.Actor;
import Util.StdDraw;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Gig on 4/3/2017.
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

    private class GameMapStorage implements Serializable {
        //priva
        private GameMapStorage(GameMap gameMap) {

        }
    }

    public GameMap(String filename) {
        if (!filename.contains(".gm"))
            filename += ".gm";
        try {
            FileInputStream fis = new FileInputStream("src/data/Maps/" + filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            GameMap gm = (GameMap) ois.readObject();

            this.mapGrid = gm.mapGrid;
            this.maxX = gm.maxX;
            this.maxY = gm.maxY;
            this.image = gm.image;
            this.actors = gm.actors;

            System.out.println(mapGrid.toString());
            System.out.println(maxX);
            System.out.println(maxY);


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

    public void draw() {
        StdDraw.picture(maxX / 2, maxY / 2, "src/img/Maps/" + image, maxX, maxY);
    }
}
