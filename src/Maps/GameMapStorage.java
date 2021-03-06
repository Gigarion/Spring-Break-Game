package Maps;

import Actors.*;
import Projectiles.Projectile;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Gig on 4/4/2017.
 * A serializeable storage class for GameMaps,
 * an actual gameMap can be extracted from it.
 * Nice and secure. gotta do this for players and actors too
 */
public class GameMapStorage implements Serializable {
    MapGrid mapGrid;
    int maxX, maxY;
    String image;
    public LinkedList<ActorStorage> actors;
    GameMapStorage(GameMap gameMap) {
        this.mapGrid = gameMap.getMapGrid();
        this.mapGrid.setPlayer(null);
        this.maxX = gameMap.getMaxX();
        this.maxY = gameMap.getMaxY();
        this.image = gameMap.getImage();
        this.actors = new LinkedList<>();
        for (Actor a : gameMap.getActors()) {
            ActorStorage toStore = ActorStorage.getActorStore(a);
            this.actors.add(toStore);
        }
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
