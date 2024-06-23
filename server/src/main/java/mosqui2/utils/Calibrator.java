package mosqui2.utils;

import mosqui2.view.View;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_highgui;
import org.bytedeco.opencv.opencv_imgproc.Vec3fVector;
import mosqui2.detection.ObjectRecog;
import org.bytedeco.opencv.opencv_core.*;

import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_highgui.destroyAllWindows;
import static org.bytedeco.opencv.global.opencv_highgui.destroyWindow;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_stitching.overlapRoi;

public class Calibrator {

    private static Scalar lowerBound;
    private static double dp, minDist, param1, param2;
    private static int minRadius, maxRadius;

    public static void setGrabber(FrameGrabber grabber) {
        Calibrator.grabber = grabber;
    }

    private static FrameGrabber grabber;

    public static double[] calibrate(View view) {
        System.out.println("Calibrating...");

        // Calculate optimal parameters based on data
        lowerBound = calculateOptimalLowerBound();
        double[] circleParams = gridSearch(view);
        dp = circleParams[0];
        minDist = circleParams[1];
        param1 = circleParams[2];
        param2 = circleParams[3];
        minRadius = (int) circleParams[4];
        maxRadius = (int) circleParams[5];

        //System.out.println("Calibration complete. New lowerBound: " + lowerBound + ", New HoughCircle parameters: dp=" + dp + ", minDist=" + minDist + ", param1=" + param1 + ", param2=" + param2 + ", minRadius=" + minRadius + ", maxRadius=" + maxRadius);
        System.out.println(Arrays.toString(circleParams));
        return circleParams;
    }

    private static Mat[] collectData(ObjectRecog objectRecog) {
        // Placeholder for function that collects relevant data for calibration
        // This could involve running the detection algorithm on a set of test images, for example
        return new Mat[0];
    }

    private static Scalar calculateOptimalLowerBound() {
        // Placeholder for function that calculates the optimal lowerBound based on collected data
        // This could involve some sort of statistical analysis or machine learning
        return new Scalar(0, 0, 0, 0);
    }

    //HoughCircles(gray, circles, HOUGH_GRADIENT, 1, 20, 200, 40, 1, 40);

    private static double[] calculateOptimalCircleParams(View view) {
        // Placeholder for function that calculates the optimal HoughCircle parameters based on collected data
        // This could involve some sort of statistical analysis or machine learning
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Mat image;
        Mat gray = new Mat();
        Mat otsu = new Mat();
        RectVector rois = new RectVector();
        Size kernel = new Size(3, 3);
        Vec3fVector circles = new Vec3fVector();
        double dp = 1, minDist = 20, param1 = 200, param2 = 40;
        int minRadius = 1, maxRadius = 40;

        try {
            grabber.restart();
            //while (circles.size() != 2) {
            image = converter.convert(grabber.grab());
            opencv_highgui.selectROIs("ROI", image, rois, true, false, false);
            destroyAllWindows();
            System.out.println("ROIS: " + rois.size());
            image = converter.convert(grabber.grab());
            cvtColor(image, gray, COLOR_BGR2GRAY);
            GaussianBlur(gray, gray, kernel, 2);
            double ostu = threshold(gray, otsu, 0, 255, THRESH_BINARY + THRESH_OTSU);
            //adaptiveThreshold(gray, otsu, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 13, 2);
            HoughCircles(gray, circles, HOUGH_GRADIENT, dp, minDist, ostu, param2, minRadius, maxRadius);
            for (int i = 0; i < circles.size(); i++) {
                Point3f circle = circles.get(i);
                Point center = new Point(Math.round(circle.get(0)), Math.round(circle.get(1)));
                int radius = Math.round(circle.get(2));
                circle(gray, center, radius, new Scalar(0, 0, 255, 0), 2, LINE_8, 0);
            }
            view.showImage(converter.convert(image));
            System.out.println(circles.size());
            // }
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{dp, minDist, param1, param2, minRadius, maxRadius};
    }

    public static double[] gridSearch(View view) {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Mat image;
        Mat circleMat = new Mat();
        Mat gray = new Mat();
        Mat otsu = new Mat();
        Mat bestScreenshot = null;
        RectVector desiredRois = new RectVector();
        Size kernel = new Size(3, 3);
        Vec3fVector circles = new Vec3fVector();
        double dp = 1, minDist = 20, param1 = 200, param2 = 40;
        int minRadius = 1, maxRadius = 40;
        double[] bestParams = new double[6];
        double bestScore = 0;
        double score = 0;
        try {
            grabber.restart();
            image = converter.convert(grabber.grab());
            opencv_highgui.selectROIs("ROI", image, desiredRois, true, false, false);
            destroyAllWindows();
            System.out.println(desiredRois.size() + " balls selected.");
            //System.out.println(opencv_highgui.selectROI("ROI", image).x());
            //destroyAllWindows();
            image = converter.convert(grabber.grab());
            cvtColor(image, gray, COLOR_BGR2GRAY);
            GaussianBlur(gray, gray, kernel, 2);
            double ostu = threshold(gray, otsu, 0, 255, THRESH_BINARY + THRESH_OTSU);
            for (int rounds = 1; rounds < 4; rounds++) {
                for (dp = 1; dp < 3; dp++) {
                    for (param2 = 1; param2 < 100; param2 += 5) {
                        //for (minRadius = 1; minRadius < 50; minRadius += 5) {
                        for (maxRadius = 10; maxRadius < 30; maxRadius += 5) {
                            HoughCircles(gray, circles, HOUGH_GRADIENT, dp, minDist, ostu, param2, minRadius, maxRadius);
                            //score = circles.size();
                            RectVector rois = new RectVector();
                            Rect[] roisArray = new Rect[(int) circles.size()];
                            circleMat = image.clone();
                            for (int i = 0; i < circles.size(); i++) {
                                Point3f circle = circles.get(i);
                                Point center = new Point(Math.round(circle.get(0)), Math.round(circle.get(1)));
                                int radius = Math.round(circle.get(2));
                                roisArray[i] = new Rect(center.x() - radius, center.y() - radius, radius * 2, radius * 2);
                                circle(circleMat, center, radius, new Scalar(0, 0, 255, 0), 2, LINE_8, 0);
                            }
                            rois.put(roisArray);
                            //System.out.println("rois size: " + rois.size() + "circles size: " + circles.size());
                            view.showImage(converter.convert(circleMat));
                            if (circles.size() == desiredRois.size()) {
                                for (int i = 0; i < desiredRois.size(); i++) {
                                    Rect desiredRoi = desiredRois.get(i);
                                    Rect overlap = new Rect();
                                    for (int j = 0; j < rois.size(); j++) {
                                        Rect roi = rois.get(j);
                                        if (overlapRoi(roi.tl(), desiredRoi.tl(), roi.size(), desiredRoi.size(), overlap)) {
                                            score += overlap.area();
                                            break;
                                        }
                                    }
                                }
                                //System.out.println("Score: " + score);
                                if (score > bestScore) {
                                    bestScore = score;
                                    score = 0;
                                    bestParams = new double[]{dp, minDist, ostu, param2, minRadius, maxRadius};
                                    bestScreenshot = circleMat.clone();
                                }
                            }
                        }
                        //System.out.println("Param2: " + param2);
                        //}
                    }
                }
                System.out.println("Round " + rounds + " complete.");
            }
            System.out.println("Best score: " + bestScore);
            view.showImage(converter.convert(bestScreenshot));
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bestParams;
    }
}