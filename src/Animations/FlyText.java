package Animations;

import Util.StdDraw;

import java.awt.*;

/**
 * Created by Gig on 3/27/2017.
 * TODO: implement fonts in StdDraw
 * TODO: also blending, but that'll take longer
 */
public class FlyText extends Animation {
    private String text;
    private int startFrame;
    private Color color;
    public FlyText(double srcX, double srcY, String text) {
        super(srcX, srcY, 30);
        this.text = text;
        startFrame = -1;
    }

    public FlyText(double srcX, double srcY, String text, Color color) {
        super(srcX, srcY, 30);
        this.text = text;
        startFrame = -1;
        this.color = color;
    }

    public void draw(int frame) {
        if (startFrame == -1)
            startFrame = frame;
        int relFrame = frame - startFrame;
        if (color != null)
            StdDraw.setPenColor(color);
        StdDraw.text(x, y + relFrame, text);
        StdDraw.setPenColor();
        ttl--;
    }
}
