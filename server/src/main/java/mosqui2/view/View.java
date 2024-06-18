package mosqui2.view;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class View extends CanvasFrame {
    public View(FrameGrabber grabber, ActionListener listener) {
        super("Mosqui2.0", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setCanvasScale(1.0);
        add(new ContentPanel(new CamFeed(canvas), new Menu(listener)));
        pack();
        //setIconImage(new ImageIcon("/home/zabell/IdeaProjects/CDIO/server/src/main/resources/Mosqui2.0.png").getImage());
    }

    @Override
    protected void initCanvas(boolean fullScreen, DisplayMode displayMode, double gamma) {
        super.initCanvas(fullScreen, displayMode, gamma);
        getContentPane().remove(canvas);
    }
}
