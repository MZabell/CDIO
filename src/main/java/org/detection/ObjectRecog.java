package org.detection;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.Vec3fVector;

import javax.swing.*;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class ObjectRecog {

    int scan() {

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("src/main/resources/video.mp4");
        CanvasFrame canvas = new CanvasFrame("Object Detection", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Mat image;
        Mat gray = new Mat();
        Vec3fVector circles = new Vec3fVector();
        Mat lower = new Mat(1, 1, CV_8UC4, new Scalar(130, 130, 130, 0));
        Mat upper = new Mat(1, 1, CV_8UC4, new Scalar(255, 255, 255, 0));
        try {
            grabber.start();
            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                image = converter.convert(frame);
                cvtColor(image, gray, CV_BGR2GRAY);
                GaussianBlur(gray, gray, new Size(7, 7), 2);

                HoughCircles(gray, circles, HOUGH_GRADIENT, 1, 20, 200, 40, 1, 0);
                for (int i = 0; i < (int) circles.size(); i++) {
                    Point3f circle = circles.get(i);
                    Point center = new Point(Math.round(circle.get(0)), Math.round(circle.get(1)));
                    int radius = Math.round(circle.get(2));

                    Rect roi = new Rect(center.x() - radius, center.y() - radius, radius * 2, radius * 2);

                    Mat roiMat;
                    try {
                        roiMat = new Mat(image, roi);
                    } catch (RuntimeException e) {
                        continue;
                    }
                    Mat color = new Mat(roiMat.size(), CV_8UC3, mean(roiMat));
                    inRange(color, lower, upper, color);
                    if (countNonZero(color) > 0) {
                        circle(image, center, radius, new Scalar(0, 255, 0, 0), 1, LINE_8, 0);
                    }
                }
                frame = converter.convert(image);
                canvas.showImage(frame);
            }
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}