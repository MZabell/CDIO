package org.detection;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.Vec3fVector;
import org.bytedeco.opencv.opencv_tracking.TrackerCSRT;

import javax.swing.*;
import javax.swing.Timer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_stitching.overlapRoi;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_DSHOW;

public class ObjectRecog {

    public ArrayBlockingQueue<DetectedObject> getQueue() {
        return queue;
    }

    private final PropertyChangeSupport support;

    private ArrayBlockingQueue<DetectedObject> queue;
    private Rect lockZone;
    OpenCVFrameGrabber grabber;

    public void setCommandX(String commandX) {
        support.firePropertyChange("CommandX", this.commandX, commandX);
        this.commandX = commandX;
    }
    private String commandX;

    public void setCommandY(String commandY) {
        support.firePropertyChange("CommandY", this.commandY, commandY);
        this.commandY = commandY;
    }

    private String commandY;

    public void setCommandZ(String commandZ) {
        support.firePropertyChange("CommandZ", this.commandZ, commandZ);
        this.commandZ = commandZ;
    }

    private String commandZ;

    public boolean isLocked() {
        return isLocked;
    }

    private boolean isLocked = false;

    public boolean isSearching() {
        return isSearching;
    }

    public void setSearching(boolean searching) {
        isSearching = searching;
    }

    private boolean isSearching = true;

    Timer lock;

    Timer unlock;

    int index = 0;

    public ObjectRecog() {
        grabber = new OpenCVFrameGrabber(0);
        support = new PropertyChangeSupport(this);
        lock = new Timer(5000, e -> {
            setCommandX("DOWN");
        });
        unlock = new Timer(10000, e -> {
            setCommandX("TEST");
            isLocked = false;
            System.out.println("UNLOCKED");
            System.out.println(queue.size());
            isSearching = true;
        });

        lock.setRepeats(false);
        unlock.setRepeats(false);
    }

    void scan() {

        TrackerCSRT.Params params = new TrackerCSRT.Params();
        params.psr_threshold(0.05f);
        ArrayList<TrackerCSRT> trackers = new ArrayList<>();
        ArrayList<TrackerCSRT> trackersToRemove = new ArrayList<>();
        ArrayList<DetectedObject> detectedObjects = new ArrayList<>();
        MatVector contours = new MatVector();
        /*queue = new PriorityBlockingQueue<>(10, (o1, o2) -> {
            if (o1.getIndex() < o2.getIndex())
                return 1;
            else if (o1.getIndex() > o2.getIndex())
                return -1;
            return 0;
        });*/
        //queue = new PriorityBlockingQueue<>(10);
        queue = new ArrayBlockingQueue<>(10);
        ArrayList<DetectedObject> objectsToRemove = new ArrayList<>();

        CanvasFrame canvas = new CanvasFrame("Object Detection", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        Mat boundryMat = new Mat();
        Mat image;
        Mat gray = new Mat();
        Vec3fVector circles = new Vec3fVector();
        Mat lower = new Mat(1, 1, CV_8UC4, new Scalar(160, 180, 200, 0));
        Mat upper = new Mat(1, 1, CV_8UC4, new Scalar(240, 255, 255, 0));
        Mat lowerRed = new Mat(new Scalar(0, 0, 150, 0));
        Mat upperRed = new Mat(new Scalar(100, 100, 255, 0));
        Scalar lowerScalar = new Scalar(160, 180, 200, 0);
        boolean isOverlapping = false;

        try {
            grabber.start();
            lockZone = new Rect(grabber.getImageWidth() / 2 - 20, grabber.getImageHeight() / 2 - 50, 70, 70);
            Frame frame;
            while ((frame = grabber.grab()) != null) {
                image = converter.convert(frame);


                inRange(image, lowerRed, upperRed, boundryMat);
                findContours(boundryMat, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

                //System.out.println(contours.size());

                cvtColor(image, gray, CV_BGR2GRAY);
                GaussianBlur(gray, gray, new Size(7, 7), 2);

                HoughCircles(gray, circles, HOUGH_GRADIENT, 1, 20, 200, 40, 1, 40);
                for (int i = 0; i < (int) circles.size(); i++) {
                    Point3f circle = circles.get(i);
                    Point center = new Point(Math.round(circle.get(0)), Math.round(circle.get(1)));
                    int radius = Math.round(circle.get(2));

                    Rect roi = new Rect(center.x() - radius, center.y() - radius, radius * 2, radius * 2);

                    for (DetectedObject o : detectedObjects) {
                        Rect overlap = new Rect();
                        if (overlapRoi(roi.tl(), o.getRoi().tl(), roi.size(), o.getRoi().size(), overlap) && overlap.area() > 300) {
                            isOverlapping = true;
                            break;
                        }
                    }
                    if (isOverlapping) {
                        isOverlapping = false;
                        continue;
                    }

                    Mat roiMat;
                    try {
                        roiMat = new Mat(image, roi);
                    } catch (RuntimeException e) {
                        continue;
                    }

                    Mat color = new Mat(roiMat.size(), CV_8UC3, mean(roiMat));
                    //System.out.println(mean(color));
                    inRange(color, lower, upper, color);
                    if (countNonZero(color) > 300) {
                        DetectedObject object = new DetectedObject(roi, center, radius, color, index++);
                        if (queue.offer(object)) {
                            detectedObjects.add(object);
                            TrackerCSRT tracker = TrackerCSRT.create(params);
                            tracker.init(image, roi);
                            trackers.add(tracker);
                        }
                    }
                }
                for (DetectedObject o : detectedObjects) {
                    boolean positive = trackers.get(detectedObjects.indexOf(o)).update(image, o.getRoi());
                    o.updateCircle(image);
                    //System.out.println(mean(o.getColor()));

                    //inRange(o.getColor(), lower, upper, o.getColor());
                    if (positive && cmpScalar(mean(o.getColor()), lowerScalar)){
                        o.updateCircle(image);
                        putText(image, (int) o.getDistance() + " cm", o.getRoi().tl(), FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 0, 0, 0));
                        circle(image, o.getCenter(), o.getRadius(), new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
                    } else {
                        trackersToRemove.add(trackers.get(detectedObjects.indexOf(o)));
                        objectsToRemove.add(o);
                    }
                }

                //trackers.removeAll(trackersToRemove);
                //detectedObjects.removeAll(objectsToRemove);
                //queue.removeAll(objectsToRemove);

                rectangle(image, lockZone, new Scalar(0, 0, 255, 0));

                //drawContours(image, contours, -1, new Scalar(255, 0, 0, 0));



                if (contours.size() != 0) {
                    if (countNonZero(new Mat(boundryMat, lockZone)) > 0) {
                        checkBoundry();
                    }
                }
                //System.out.println(roi.x() + " " + roi.y());

                frame = converter.convert(image);
                canvas.showImage(frame);
                //if (queue.peek() != null)
                  //  System.out.println(queue.peek().getRoi().x());

            }
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(DetectedObject object) {
        if (!isLocked) {
            if (object.getRoi().y() < lockZone.y()) {
                setCommandY("FORWARD");
            } else if (object.getRoi().y() + object.getRoi().height() > lockZone.y() + lockZone.height()) {
                setCommandY("BACKWARD");
            } else {
                setCommandY("STOP");
            }

            if (object.getRoi().x() < lockZone.x()) {
                setCommandX("LEFT");
            } else if (object.getRoi().x() + object.getRoi().width() > lockZone.x() + lockZone.width()) {
                setCommandX("RIGHT");
            } else {
                setCommandX("STOP");
            }


            if (commandX.equals("STOP") && commandY.equals("STOP")) {
                //setCommandX("OPEN");
                isLocked = true;
                System.out.println("LOCKED");
                setCommandY("FORWARDCTRLD");

                lock.restart();
                unlock.restart();
            }
        }


    }

    public boolean cmpScalar(Scalar s1, Scalar s2) {
        return !(s1.blue() < s2.blue()) && !(s1.green() < s2.green()) && !(s1.red() < s2.red());
    }

    public void checkBoundry() {
        System.out.println("Boundry breached " + queue.size());
        queue.clear();
        if (commandX.equals("LEFT")) {
            setCommandX("RIGHT");
        } else if (commandX.equals("RIGHT")) {
            setCommandX("LEFT");
        }
    }

    public void testRails() {
        setCommandY("STOP");
        setCommandY("FORWARD");
        Timer timer = new Timer(4000, e -> {
            if (commandY.equals("FORWARD"))
                setCommandY("BACKWARD");
            else if (commandY.equals("BACKWARD")) {
                setCommandY("FORWARD");
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}