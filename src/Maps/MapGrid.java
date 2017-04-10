package Maps;

import Actors.Actor;
import Actors.Mob;
import Actors.Player;
import Util.StdDraw;

import java.awt.*;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Gig on 4/2/2017.
 * used for blocking movement through areas
 * Rather than having to make a bunch of invisible impassable actors
 */
public class MapGrid implements Serializable {

    // heights that things can be.  Actors with a
    // goesOver value >= a height can traverse that
    // height as if it were not blocked.
    public static final char GROUND_HEIGHT = 0;
    public static final char HALF_HEIGHT = 1;
    public static final char FULL_HEIGHT = 2;
    public static final char ABS_HEIGHT = 3;

    private int boxSize; // size in pixels of the impassability box
    private char[][] grid;
    private int maxBoxX, maxBoxY;
    private boolean showGrid;
    private boolean showBoxes;
    private boolean showPlayerBoxes;
    private Player player;

    public MapGrid(int maxLogX, int maxLogY, int boxSize) {
        this.maxBoxX = maxLogX / boxSize;
        this.maxBoxY = maxLogY / boxSize;
        this.grid = new char[maxBoxX][maxBoxY];
        this.boxSize = boxSize;
        this.showGrid = false;
        this.showBoxes = false;
    }


    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setShowGrid(boolean show) {
        this.showGrid = show;System.out.println("showing " + show);
    }

    public void setShowBoxes(boolean show) {
        this.showBoxes = show;
    }

    // returns whether or not the location is inside a blocked box
    public boolean isBlocked(double x, double y, int height) {
        int gridX = convertIndex(x);
        int gridY = convertIndex(y);
        return grid[gridX][gridY] > height;
    }

    public void block(Actor a) {
        for (Point p : getBoxes(a)) {
            block(p, a.getPassesHeight());
        }
    }

    public void block(double x, double y, char height) {
        block(new Point(convertIndex(x), convertIndex(y)), height);
    }

    private void block(Point p, char height) {
        if (p.x >= maxBoxX || p.y >= maxBoxY || p.x < 0 || p.y < 0)
            return;
        grid[p.x][p.y] = height;
    }

    public void unblock(Actor a) {
        Iterable<Point> toUnblock = getBoxes(a);
        for (Point p : toUnblock) {
            unblock(p);
        }
    }

    public void unblock(double x, double y) {
        unblock(new Point(convertIndex(x), convertIndex(y)));
    }

    private void unblock(Point p) {
        grid[p.x][p.y] = GROUND_HEIGHT;
    }

    public void draw() {
        // draw individual boxes
        if (showBoxes) {
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    double x = (boxSize * r) + boxSize / 2.0;
                    double y = (boxSize * c) + boxSize / 2.0;
                    switch (grid[r][c]) {
                        case 0:
                            continue;
                        case 1:
                            StdDraw.setPenColor(StdDraw.BLUE);
                            break;
                        case 2:
                            StdDraw.setPenColor(StdDraw.PRINCETON_ORANGE);
                            break;
                        case 3:
                            StdDraw.setPenColor();
                            break;
                        default:
                            StdDraw.setPenColor(StdDraw.RED);
                            break;
                    }
                    StdDraw.filledRectangle(x, y, boxSize / 2, boxSize / 2);
                    StdDraw.setPenColor();
                }
            }
        }
        if (player != null && showPlayerBoxes) {
            for (Point p : getBoxes(player)) {
                StdDraw.setPenColor(StdDraw.RED);
                StdDraw.filledRectangle((boxSize * p.getX()) + boxSize / 2, (boxSize * p.getY()) + boxSize / 2, boxSize / 2, boxSize / 2);
                StdDraw.setPenColor();
            }
        }
        // draw gridlines
        if (showGrid) {
            for (int i = 0; i < maxBoxX; i++) {
                StdDraw.line(i * boxSize, 0, i * boxSize, maxBoxY * boxSize);
            }
            for (int i = 0; i < maxBoxY; i++) {
                StdDraw.line(0, i * boxSize, maxBoxX * boxSize, i * boxSize);
            }
        }
    }

    private int convertIndex(double index) {
        return (int) (index / boxSize);
    }

    private Iterable<Point> getBoxes(Actor a) {
        LinkedList<Point> toReturn = new LinkedList<>();
        int startX = convertIndex(a.getX());
        int startY = convertIndex(a.getY());

        int minX = Math.max(0, convertIndex(a.getX() - a.getR()));
        int minY = Math.max(0, convertIndex(a.getY() - a.getR()));

        int maxX = Math.min(maxBoxX, convertIndex(a.getX() + a.getR()));
        int maxY = Math.min(maxBoxY, convertIndex(a.getY() + a.getR()));

        // add cross and fill to half diffs
        // pretty much a box i think
        int halfDiffX = (maxX - minX) / 2;
        int halfDiffY = (maxY - minY) / 2;
        for (int i = minX; i <= maxX; i++) {
            toReturn.add(new Point(i, startY));
            for (int j = startY; j < halfDiffY; j++) {
                toReturn.add(new Point(i, startY + halfDiffY));
                toReturn.add(new Point(i, startY - halfDiffY));
            }
        }
        for (int i = minY; i <= maxY; i++) {
            toReturn.add(new Point(startX, i));
            for (int j = startX; j < halfDiffX; j++) {
                toReturn.add(new Point(i, startX + halfDiffX));
                toReturn.add(new Point(i, startX - halfDiffX));
            }
        }
        return toReturn;
    }

    public boolean validMove(double destX, double destY, Actor a) {
        Mob test = new Mob(-1, destX, destY, a.getR(), 100);
        char maxHeight = a.getPassesHeight();
        if (destX < 0 || destY < 0 || destX > maxBoxX * boxSize || destY > maxBoxY * boxSize)
            return false;
        Iterable<Point> newBoxes = getBoxes(test);
        for (Point p : newBoxes) {
            if (p.x >= grid.length || p.y >= grid[0].length || p.x < 0 || p.y < 0)
                return false;
            if (grid[p.x][p.y] > maxHeight)
                return false;
        }
        return true;
    }

    public void setShowPlayerBoxes(boolean showPlayerBoxes) {
        this.showPlayerBoxes = showPlayerBoxes;
    }

    // string format = {maxX}/{maxY}/{boxSize}/x,y/x,y/
    // probably never going to be used because GameMapStorage
    public static MapGrid loadFromString(String initString) {
        String[] units = initString.split("/");
        int maxX = Integer.parseInt(units[0]);
        int maxY = Integer.parseInt(units[1]);
        int boxSize = Integer.parseInt(units[2]);
        MapGrid mapGrid = new MapGrid(maxX, maxY, boxSize);
        for (int i = 3; i < units.length; i++) {
            String[] coords = units[i].split(",");
            mapGrid.block(new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])), coords[2].charAt(0));
        }
        return mapGrid;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(maxBoxX);
        sb.append("/");
        sb.append(maxBoxY);
        sb.append("/");
        sb.append(boxSize);
        for (int i = 0; i < maxBoxX; i++) {
            for (int c = 0; c < maxBoxY; c++) {
                if (grid[i][c] > 0) {
                    sb.append("/");
                    sb.append(i);
                    sb.append(',');
                    sb.append(c);
                    sb.append(',');
                    sb.append(Character.toString(grid[i][c]));
                }
            }
        }
        return sb.toString();
    }
}
