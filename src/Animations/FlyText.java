package Animations;

import Util.StdDraw;

/**
 * Created by Gig on 3/27/2017.
 * TODO: make this class work, good small side branch
 * TODO: implement fonts in StdDraw
 * TODO: also blending, but that'll take longer
 */
public class FlyText extends Animation {
    private String text;
    private int startFrame;
    public FlyText(double srcX, double srcY, String text) {
        super(srcX, srcY, 900);
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
