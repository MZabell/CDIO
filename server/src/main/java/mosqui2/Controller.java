package mosqui2;

import mosqui2.detection.ObjectRecog;
import mosqui2.detection.Server;
import mosqui2.utils.Calibrator;
import mosqui2.utils.VideoGrabber;
import mosqui2.view.View;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Class for providing ObjectRecog and View with neccesary values via dependency injection
public class Controller {

    ObjectRecog objectRecog;

    View view;

    public Controller() {
        VideoGrabber grabberSingleton = VideoGrabber.INSTANCE;
        grabberSingleton.setGrabber(new OpenCVFrameGrabber(0));
        Calibrator.setGrabber(grabberSingleton.getGrabber());
        view = new View(grabberSingleton.getGrabber(), new MenuButtonListener());
        objectRecog = new ObjectRecog(grabberSingleton.getGrabber(), view);
        Server server = new Server(objectRecog);
        System.exit(0);
    }

    private class MenuButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "Calibrate":
                    objectRecog.setCircleParams(Calibrator.calibrate(view));
                    break;
                case "Scan":
                    objectRecog.startScan();
                    view.getFeed().setStatus(true);
                    break;
                case "Map":
                    objectRecog.startMap();
                    break;
                case "Start":
                    objectRecog.startSearch();
                    break;
                case "Stop":
                    objectRecog.stop();
                    view.getFeed().setStatus(false);
                    break;
                case "Exit":
                    System.exit(0);
                    break;
            }
        }
    }
}
