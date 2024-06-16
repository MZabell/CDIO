package org.utils;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public enum VideoGrabber {
    INSTANCE;

    public FrameGrabber getGrabber() {
        return grabber;
    }

    public void setGrabber(FrameGrabber grabber) {
        this.grabber = grabber;
    }

    FrameGrabber grabber;

}
