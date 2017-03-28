package Animations;

import Util.StdDraw;

/**
 * Created by Gig on 3/27/2017.
 */
public class FlyText extends Animation{
    private double srcX, srcY;
    private String text;
    private int startFrame;
    public FlyText(double srcX, double srcY, String text) {
        super(srcX, srcY, 900);
        this.srcX = srcX;
        this.srcY = srcY;
        this.text = text;
        startFrame = -1;
    }

    public void draw(int frame) {
        if (startFrame == -1)
            startFrame = frame;
        int relFrame = frame - startFrame;
        StdDraw.text(x, y + relFrame / 2.0, text);
    }
}
