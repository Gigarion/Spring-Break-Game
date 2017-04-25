package Equipment;

import Util.StdDraw;

/**
 * Created by Gig on 4/24/2017.
 * a stack of one item type, which may be in a player's inventory
 */
public abstract class Item {
    private int id;
    private double xScale, yScale;
    private double weight;   // weight of an individual item in the stack
    private String invImage; // image to be shown in inventory
    private int count, maxCount; // count in this stack
    public Item(int id, double weight, int maxCount) {
        this.id = id;
        xScale = 10;
        yScale = 10;
        this.maxCount = maxCount;
        this.weight = weight;
    }

    // attempt to add @param count of the item to this stack,
    // returns the number of items added
    public int add(int count) {
        while (this.count > maxCount && count > 0) {
            this.count++;
            count--;
        }
        return count;
    }

    public int getId() {return this.id;}
    public int getCount() {return this.count;}


    public void setCount(int count) {
        this.count = count;
    }
    public void setInvImage(String imageStr) {this.invImage = imageStr;}
    double getWeight() {return this.weight * this.count;}

    public void drawItem(double x, double y) {
        if (invImage == null)
            StdDraw.filledCircle(x, y, 5);
        try {
            StdDraw.picture(x, y, "src/img/Actors/" + invImage, xScale, yScale);
        } catch (Exception e) {
            StdDraw.picture(x, y, "img/Actors/" + invImage, xScale, yScale);
        }
    }
}
