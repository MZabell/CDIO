package mosqui2.detection;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;

import static org.bytedeco.opencv.global.opencv_core.*;

public class DetectedObject {

    private final Rect roi;

    public Rect getRoi() {
        return roi;
    }

    public Point getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    private Point center;
    private int radius;
    private Mat color;

    public Mat getColor() {
        return color;
    }

    public void setColor(Mat color) {
        this.color = color;
    }

    public double getDistance() {
        return distance;
    }

    private double distance;

    public int getIndex() {
        return index;
    }

    private int index;

    public DetectedObject(Rect roi, Point center, int radius, Mat color, int index) {
        this.roi = roi;
        this.center = center;
        this.radius = radius;
        this.color = color;
        this.index = index;
    }

    public void updateCircle(Mat image) {
        center = new Point(roi.x() + roi.width() / 2, roi.y() + roi.height() / 2);
        radius = roi.width() / 2;

        distance = (4.0 * 545) / (radius * 2);

        try {
            color = new Mat(roi.size(), CV_8UC3, mean(new Mat(image, roi)));
        } catch (RuntimeException ignored) {
        }
    }
}