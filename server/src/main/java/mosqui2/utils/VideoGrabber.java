package mosqui2.utils;

import org.bytedeco.javacv.FrameGrabber;

// Enum for singleton implementation of FrameGrabber
public enum VideoGrabber {
    INSTANCE;

    FrameGrabber grabber;

    public FrameGrabber getGrabber() {
        return grabber;
    }

    public void setGrabber(FrameGrabber grabber) {
        this.grabber = grabber;
    }

}
