package org.example;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;

public class ObjectRecog {
    public static void main(String[] args) {
        OpenCV.loadShared();

        Mat image = Imgcodecs.imread("src/main/resources/image.jpg");
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        CascadeClassifier faceCascade = new CascadeClassifier("src/main/resources/haarcascade_frontalface_alt.xml");
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayImage, faces);

        List<Rect> faceList = new ArrayList<>(faces.toList());
        for (Rect face : faceList) {
            Imgproc.rectangle(image, face, new Scalar(0, 255, 0), 3);
        }

        Imgcodecs.imwrite("src/main/resources/detected_faces.jpg", image);
    }
}