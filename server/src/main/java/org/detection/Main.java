package org.detection;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.utils.Calibrator;
import org.utils.VideoGrabber;
import org.view.View;

public class Main {

    public static void main(String[] args) {
        VideoGrabber grabberSingleton = VideoGrabber.INSTANCE;
        grabberSingleton.setGrabber(new OpenCVFrameGrabber(1));
        Calibrator.setGrabber(grabberSingleton.getGrabber());
        View view = new View(grabberSingleton.getGrabber());
        ObjectRecog objectRecog = new ObjectRecog(grabberSingleton.getGrabber(), view);
        Server server = new Server(objectRecog);
        /*PIDController pidController = new PIDController(0.1, 0.1, 0.1);
        int i = 0;
        while (true) {
            i++;
            System.out.println(pidController.calculate(i));
        }*/
        System.exit(0);
    }
}
