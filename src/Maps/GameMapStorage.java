package Maps;

import Actors.Actor;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Gig on 4/4/2017.
 */
public class GameMapStorage implements Serializable {
    MapGrid mapGrid;
    int maxX, maxY;
    String image;
    public LinkedList<Actor> actors;
    GameMapStorage(GameMap gameMap) {
        this.mapGrid = gameMap.getMapGrid();
        this.maxX = gameMap.getMaxX();
        this.maxY = gameMap.getMaxY();
        this.image = gameMap.getImage();
        this.actors = gameMap.getActors();
    }

    // extracts the gamemap without actors attached,
    // used for initializing the ClientEngine GameMaps,
    // since they don't need actor access
    public GameMap extractGameMap() {
        GameMap toReturn =  new GameMap(maxX, maxY, image);
        toReturn.setMapGrid(mapGrid);
        return  toReturn;
    }
}
