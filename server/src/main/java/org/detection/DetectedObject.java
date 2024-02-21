package org.detection;

import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;

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

    public double getDistance() {
        return distance;
    }

    private double distance;

    public DetectedObject(Rect roi, Point center, int radius) {
        this.roi = roi;
        this.center = center;
        this.radius = radius;
    }

    public void updateCircle() {
        center = new Point(roi.x() + roi.width() / 2, roi.y() + roi.height() / 2);
        radius = roi.width() / 2;

        distance = (3.5 * 545) / (radius * 2);
    }
}
