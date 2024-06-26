package mosqui2.utils;

import mosqui2.view.View;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_highgui;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.Vec3fVector;

import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_highgui.destroyAllWindows;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_stitching.overlapRoi;

// Utility class for calibrating detection algorithm
public class Calibrator {

    private static Scalar lowerBound;
    private static double dp, minDist, param1, param2;
    private static int minRadius, maxRadius;
    private static FrameGrabber grabber;

    public static void setGrabber(FrameGrabber grabber) {
        Calibrator.grabber = grabber;
    }

    public static double[] calibrate(View view) {

        lowerBound = calculateOptimalLowerBound();
        double[] circleParams = gridSearch(view);
        dp = circleParams[0];
        minDist = circleParams[1];
        param1 = circleParams[2];
        param2 = circleParams[3];
        minRadius = (int) circleParams[4];
        maxRadius = (int) circleParams[5];

        System.out.println("New Hough Transform Params: " + Arrays.toString(circleParams));
        return circleParams;
    }

    private static Scalar calculateOptimalLowerBound() {
        // Not implemented
        return new Scalar(0, 0, 0, 0);
    }


    // Gridsearch to find the best parameters for Hough transform
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
            System.out.println("Click and drag to select balls. Press Enter to continue. Press Escape when done.");
            opencv_highgui.selectROIs("Select number of ROIs", image, desiredRois, true, false, false);
            destroyAllWindows();
            System.out.println(desiredRois.size() + " balls selected.");
            image = converter.convert(grabber.grab());
            cvtColor(image, gray, COLOR_BGR2GRAY);
            GaussianBlur(gray, gray, kernel, 2);
            double ostu = threshold(gray, otsu, 0, 255, THRESH_BINARY + THRESH_OTSU);
            for (int rounds = 1; rounds < 4; rounds++) {
                for (dp = 1; dp < 3; dp++) {
                    for (param2 = 1; param2 < 100; param2 += 5) {
                        for (maxRadius = 10; maxRadius < 30; maxRadius += 5) {
                            HoughCircles(gray, circles, HOUGH_GRADIENT, dp, minDist, ostu, param2, minRadius, maxRadius);
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
                                if (score > bestScore) {
                                    bestScore = score;
                                    score = 0;
                                    bestParams = new double[]{dp, minDist, ostu, param2, minRadius, maxRadius};
                                    bestScreenshot = circleMat.clone();
                                }
                            }
                        }
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