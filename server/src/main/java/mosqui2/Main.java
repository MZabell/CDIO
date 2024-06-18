package mosqui2;

import org.bytedeco.javacv.OpenCVFrameGrabber;
import mosqui2.detection.ObjectRecog;
import mosqui2.detection.Server;
import mosqui2.utils.Calibrator;
import mosqui2.utils.VideoGrabber;
import mosqui2.view.View;

public class Main {

    public static void main(String[] args) {
        new Controller();
    }
}
