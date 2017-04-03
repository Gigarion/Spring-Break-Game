package Util;

import Gui.UserBox;

/**
 * Created by Gig on 4/2/2017.
 */
public class MapBuilder {
    private MapGrid mapGrid;
    private UserBox userBox;

    public MapBuilder(int maxX, int maxY, int boxSize) {
        this.mapGrid = new MapGrid(maxX, maxY, boxSize);

    }

    public void mark(double x, double y) {
        mapGrid.block(x, y);
    }
}
