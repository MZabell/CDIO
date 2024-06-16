package org.view;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

public class View extends CanvasFrame {
    public View(FrameGrabber grabber) {
        super("Mosqui2.0", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setCanvasScale(1.0);
        add(new ContentPanel(new CamFeed(canvas), new Menu()));
        pack();
        //setIconImage(new ImageIcon("/home/zabell/IdeaProjects/CDIO/server/src/main/resources/Mosqui2.0.png").getImage());
    }

    @Override
    protected void initCanvas(boolean fullScreen, DisplayMode displayMode, double gamma) {
        super.initCanvas(fullScreen, displayMode, gamma);
        getContentPane().remove(canvas);
    }
}
