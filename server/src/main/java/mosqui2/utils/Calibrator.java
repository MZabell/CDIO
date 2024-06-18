package mosqui2.utils;

import mosqui2.view.View;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_imgproc.Vec3fVector;
import mosqui2.detection.ObjectRecog;
import org.bytedeco.opencv.opencv_core.*;

import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

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
        double[] circleParams = calculateOptimalCircleParams(view);
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
        Size kernel = new Size(7, 7);
        Vec3fVector circles = new Vec3fVector();
        double dp = 1, minDist = 20, param1 = 200, param2 = 40;
        int minRadius = 1, maxRadius = 40;

        try {
            grabber.restart();
            //while (circles.size() != 2) {
                image = converter.convert(grabber.grab());
                cvtColor(image, gray, COLOR_BGR2GRAY);
                GaussianBlur(gray, gray, kernel, 2);
                HoughCircles(gray, circles, HOUGH_GRADIENT, dp, minDist, param1, param2, minRadius, maxRadius);
                view.showImage(converter.convert(gray));
                System.out.println(circles.size());
           // }
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{dp, minDist, param1, param2, minRadius, maxRadius};
    }
}