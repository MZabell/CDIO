package org.detection;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class ObjectRecog {

    int scan() {
        OpenCV.loadLocally();

        VideoCapture capture = new VideoCapture("src/main/resources/video.mp4");

        Mat image = new Mat();
        Mat mask = new Mat();
        Scalar lower = new Scalar(130, 130, 130);
        Scalar upper = new Scalar(255, 255, 255);
        Mat circles = new Mat();

        while (capture.read(image)) {
            Imgproc.cvtColor(image, mask, Imgproc.COLOR_BGR2GRAY);

            Imgproc.GaussianBlur(mask, mask, new Size(7, 7), 2);

            Imgproc.HoughCircles(mask, circles, Imgproc.HOUGH_GRADIENT, 1.0, 25, 60, 40, 1, 150);
            for (int i = 0; i < circles.cols(); i++) {
                double[] circle = circles.get(0, i);
                Point center = new Point(Math.round(circle[0]), Math.round(circle[1]));
                int radius = (int) Math.round(circle[2]);

                Rect roi = new Rect((int) center.x - radius, (int) center.y - radius, radius * 2, radius * 2);

                Mat roiMat = new Mat(image, roi);
                Mat color = new Mat(roiMat.size(), CvType.CV_8UC3, Core.mean(roiMat));
                Core.inRange(color, lower, upper, color);
                if (Core.countNonZero(color) > 0) {
                    Imgproc.circle(image, center, 1, new Scalar(0, 100, 100), 3);
                    Imgproc.circle(image, center, radius, new Scalar(0, 255, 0), 2);
                }
            }

            HighGui.imshow("Output", image);
            HighGui.waitKey(1);
        }
        capture.release();
        return 0;
    }
}