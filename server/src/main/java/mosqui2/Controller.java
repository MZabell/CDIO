package mosqui2;

import mosqui2.detection.ObjectRecog;
import mosqui2.detection.Server;
import mosqui2.utils.Calibrator;
import mosqui2.utils.VideoGrabber;
import mosqui2.view.View;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controller {

    ObjectRecog objectRecog;

    View view;

    public Controller() {
        VideoGrabber grabberSingleton = VideoGrabber.INSTANCE;
        grabberSingleton.setGrabber(new OpenCVFrameGrabber(1));
        Calibrator.setGrabber(grabberSingleton.getGrabber());
        view = new View(grabberSingleton.getGrabber(), new MenuButtonListener());
        objectRecog = new ObjectRecog(grabberSingleton.getGrabber(), view);
        Server server = new Server(objectRecog);
        /*PIDController pidController = new PIDController(0.1, 0.1, 0.1);
        int i = 0;
        while (true) {
            i++;
            System.out.println(pidController.calculate(i));
        }*/
        System.exit(0);
    }

    private class MenuButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "Calibrate":
                    objectRecog.setCircleParams(Calibrator.calibrate(view));
                    objectRecog.scan();
                    break;
                case "Start":
                    objectRecog.mapField();
                    break;
                case "Stop":
                    objectRecog.start();
                    break;
                case "Exit":
                    break;
            }
        }
    }
}
