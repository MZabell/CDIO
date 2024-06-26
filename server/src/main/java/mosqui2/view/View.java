package mosqui2.view;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

// Class for GUI window
// Extends CanvasFrame from JavaCV dependency
public class View extends CanvasFrame {

    private final CamFeed feed;

    public View(FrameGrabber grabber, ActionListener listener) {
        super("Mosqui2.0", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setCanvasScale(1.0);
        feed = new CamFeed(canvas);
        add(new ContentPanel(feed, new Menu(listener)));
        pack();
    }

    public CamFeed getFeed() {
        return feed;
    }

    @Override
    protected void initCanvas(boolean fullScreen, DisplayMode displayMode, double gamma) {
        super.initCanvas(fullScreen, displayMode, gamma);
        getContentPane().remove(canvas);
    }
}
