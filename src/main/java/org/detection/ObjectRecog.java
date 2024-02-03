package org.detection;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import java.util.ArrayList;
import java.util.List;

public class ObjectRecog {

    int scan() {
        OpenCV.loadLocally();

        VideoCapture capture = new VideoCapture("src/main/resources/video.mp4");

        Size framesize = new Size(capture.get(3), capture.get(4));

        VideoWriter writer = new VideoWriter("src/main/resources/output.mp4", VideoWriter.fourcc('a', 'v', 'c', '1'), 30, framesize);

        Mat image = new Mat();
        Mat mask = new Mat();
        Scalar lower = new Scalar(0, 0, 100);
        Scalar upper = new Scalar(100, 30, 255);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Size kernelSize = new Size(15, 15);
        Mat kernel;

        while (capture.read(image)) {
            capture.read(image);
            Imgproc.cvtColor(image, mask, Imgproc.COLOR_BGR2HSV);

            kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, kernelSize);

            Core.inRange(mask, lower, upper, mask);
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
            kernelSize.width = 20;
            kernelSize.height = 20;
            kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, kernelSize);
            Imgproc.erode(mask, mask, kernel);
            kernelSize.width = 18;
            kernelSize.height = 18;
            kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, kernelSize);
            Imgproc.dilate(mask, mask, kernel);

            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            List<MatOfPoint> hullList = new ArrayList<>();
            for (MatOfPoint contour : contours) {
                MatOfInt hull = new MatOfInt();
                Imgproc.convexHull(contour, hull);
                Point[] contourArray = contour.toArray();
                Point[] hullPoints = new Point[hull.rows()];
                List<Integer> hullContourIdxList = hull.toList();
                for (int i = 0; i < hullContourIdxList.size(); i++) {
                    hullPoints[i] = contourArray[hullContourIdxList.get(i)];
                }
                hullList.add(new MatOfPoint(hullPoints));
            }

            for (MatOfPoint hull : hullList) {
                if (!Imgproc.isContourConvex(hull) || Imgproc.contourArea(hull) > 2000)
                    continue;
                Moments moments = Imgproc.moments(hull);
                Point center = new Point(moments.get_m10() / moments.get_m00(), moments.get_m01() / moments.get_m00());
                Imgproc.circle(image, center, (int) Math.sqrt(Imgproc.contourArea(hull) / Math.PI), new Scalar(0, 255, 0), 2);
            }

            //Imgcodecs.imwrite("src/main/resources/mask.jpg", mask);
            //Imgcodecs.imwrite("src/main/resources/output.jpg", image);
            writer.write(image);
            contours.clear();
        }
        capture.release();
        writer.release();
        return 0;
    }
}