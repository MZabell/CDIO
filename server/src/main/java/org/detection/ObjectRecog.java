package org.detection;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.Vec3fVector;
import org.bytedeco.opencv.opencv_tracking.TrackerKCF;
import org.utils.VideoGrabber;
import org.view.View;

import javax.swing.Timer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_stitching.overlapRoi;

public class ObjectRecog {

    public PriorityBlockingQueue<DetectedObject> getQueue() {
        return queue;
    }

    private final PropertyChangeSupport support;

    private PriorityBlockingQueue<DetectedObject> queue;
    private static Rect lockZone;
    private static Rect edgeUp;
    private static Rect edgeDown;
    private static Rect edgeLeft;
    private static Rect edgeRight;
    private Mat upMat, downMat, leftMat, rightMat;
    //OpenCVFrameGrabber grabber;

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    private int speed = 0;

    public void setTachoY(int tachoY) {
        //System.out.println("TachoY: " + tachoY);
        this.tachoY = tachoY;
    }

    public int getTachoY() {
        //setCommandY("GETTACHO");
        return tachoY;
    }

    private int tachoY = 0;

    public int getTachoX() {
        //setCommandX("GETTACHO");
        return tachoX;
    }

    public void setTachoX(int tachoX) {
        //System.out.println("TachoX: " + tachoX);
        this.tachoX = tachoX;
    }

    private int tachoX = 0;

    public void setCommandX(String commandX) {
        support.firePropertyChange("CommandX", this.commandX, commandX);
        this.commandX = commandX;
    }
    private String commandX = "";

    public void setCommandY(String commandY) {
        support.firePropertyChange("CommandY", this.commandY, commandY);
        this.commandY = commandY;
    }

    private String commandY = "";

    public enum SearchState {
        SearchForward {
            @Override
            public SearchState nextState() {
                previousState = SearchForward;
                //System.out.println(previousState);
                return SearchLeft;
            }

            @Override
            public void move(ObjectRecog o) {
                o.setCommandY("FORWARD");
                o.setCommandX("STOP");
            }
        },
        SearchBackward {
            @Override
            public SearchState nextState() {
                previousState = SearchBackward;
                return SearchLeft;
            }

            @Override
            public void move(ObjectRecog o) {
                o.setCommandY("BACKWARD");
                o.setCommandX("STOP");
            }
        },
        SearchLeft {
            @Override
            public SearchState nextState() {
                System.out.println(previousState);
                if (previousState == SearchForward) {
                    System.out.println("Going back");
                    previousState = SearchLeft;
                    return SearchBackward;
                } else return SearchForward;
            }

            @Override
            public void move(ObjectRecog o) {
                try {
                    synchronized (o) {
                        if (previousState == SearchForward) {
                            o.setCommandY("BACKWARD");
                        }
                        else o.setCommandY("FORWARD");
                        o.wait(500);
                        o.setCommandY("STOP");
                        o.setCommandX("LEFT");
                        o.wait(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },
        SearchRight {
            @Override
            public SearchState nextState() {
                if (previousState == SearchForward) {
                    previousState = SearchRight;
                    return SearchBackward;
                } else return SearchForward;
            }

            @Override
            public void move(ObjectRecog o) {
                try {
                    synchronized (o) {
                        if (previousState == SearchForward)
                            o.setCommandY("BACKWARD");
                        else o.setCommandY("FORWARD");
                        o.wait(500);
                        o.setCommandY("STOP");
                        o.setCommandX("RIGHT");
                        o.wait(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },
        //SearchGoal {};
        SearchWait {
            @Override
            public SearchState nextState() {
                return SearchReturn;
            }

            @Override
            public void move(ObjectRecog o) {}
        },
        SearchDone {
            @Override
            public SearchState nextState() {
                return null;
            }

            @Override
            public void move(ObjectRecog o) {
                try {
                    synchronized (o) {
                        o.setCommandX("LEFTCTRLD");
                        o.setCommandY("FORWARDCTRLD2");
                        o.wait(5000);
                        o.setCommandX("CLOSE");
                        o.wait(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },
        SearchGoal {
            @Override
            public SearchState nextState() {
                return SearchDone;
            }

            @Override
            public void move(ObjectRecog o) {
                if (o.checkBoundry(o.rightMat)) {
                    o.setCommandX("STOP");
                } else if (!Objects.equals(o.commandX, "STOP")) {
                    o.setCommandX("RIGHT");
                }

                if (o.checkBoundry(o.downMat)) {
                    o.setCommandY("STOP");
                } else if (!Objects.equals(o.commandY, "STOP")){
                    o.setCommandY("BACKWARD");
                }
            }
        },
        SearchReturn {
            @Override
            public SearchState nextState() {
                return Objects.requireNonNullElse(previousState, SearchForward);
            }

            @Override
            public void move(ObjectRecog o) {
                int tacho = o.getTachoX();


                /*if (o.commandX.equals("RIGHT")) {
                    tacho = o.getTachoX();
                } else tacho = o.getTachoX();
                System.out.println(tacho + " " + previousTacho);
                if (tacho > previousTacho + 100) {
                    o.setCommandX("RIGHT");
                } else if (tacho < previousTacho - 100) {
                    o.setCommandX("LEFT");
                } else o.setCommandX("STOP");*/
            }
        };

        public static SearchState previousState = null;
        public static int previousTacho = 0;
        public SearchState pauseSearch(ObjectRecog o) {
            previousTacho = o.getTachoX();
            System.out.println("Paused " + previousTacho);
            return SearchWait;
        }
        public abstract SearchState nextState();
        public abstract void move(ObjectRecog o);
    }

    public enum CollectState {
        NotFound {
            @Override
            public CollectState nextState() {
                return Found;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {}
        },
        Found {
            @Override
            public CollectState nextState() {
                return Engaged;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {
                if (object == null) {
                    o.setCommandY("BACKWARD");
                    return;
                }
                if (object.getRoi().y() < lockZone.y()) {
                    o.setCommandY("FORWARD");
                } else if (object.getRoi().y() + object.getRoi().height() > lockZone.y() + lockZone.height()) {
                    o.setCommandY("BACKWARD");
                } else {
                    o.setCommandY("STOP");
                }

                if (object.getRoi().x() < lockZone.x()) {
                    o.setCommandX("LEFT");
                } else if (object.getRoi().x() + object.getRoi().width() > lockZone.x() + lockZone.width()) {
                    o.setCommandX("RIGHT");
                } else {
                    o.setCommandX("STOP");
                }
            }
        },
        Engaged {
            @Override
            public CollectState nextState() {
                return Done;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {
                try {
                    synchronized (o) {
                        o.setSpeed(150);
                        o.setCommandY("FORWARDCTRLD");
                        o.wait(2000);
                        o.setSpeed(650);
                        o.setCommandX("DOWN");
                        o.wait(3000);
                    }
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
            }
        },
        Done {
            @Override
            public CollectState nextState() {
                return NotFound;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {}
        },

        Reset {
            @Override
            public CollectState nextState() {
                return NotFound;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {
            }
        };

        public abstract CollectState nextState();
        public abstract void move(ObjectRecog o, DetectedObject object);
    }
    Timer lock;

    Timer unlock;

    FrameGrabber grabber;
    View view;

    int index = 0;
    Mat boundryMat;
    MatVector contours;
    ArrayList<DetectedObject> detectedObjects;
    ArrayList<TrackerKCF> trackers;
    ArrayList<DetectedObject> objectsToRemove;
    ArrayList<TrackerKCF> trackersToRemove;
    PIDController pid;

    public ObjectRecog(FrameGrabber grabber, View view) {
        this.grabber = grabber;
        this.view = view;
        //grabber = new OpenCVFrameGrabber(1);
        support = new PropertyChangeSupport(this);
        lock = new Timer(2000, e -> {
            setCommandX("DOWN");
        });
        unlock = new Timer(5000, e -> {
            setCommandX("TEST");
            System.out.println("UNLOCKED");
            System.out.println(queue.size());
        });

        lock.setRepeats(false);
        unlock.setRepeats(false);
        pid = new PIDController(0.1, 0.1, 0.1);
    }

    void scan() {

        TrackerKCF.Params params = new TrackerKCF.Params();

        trackers = new ArrayList<>();
        trackersToRemove= new ArrayList<>();
        detectedObjects = new ArrayList<>();
        contours = new MatVector();
        /*queue = new PriorityBlockingQueue<>(10, (o1, o2) -> {
            if (o1.getIndex() < o2.getIndex())
                return 1;
            else if (o1.getIndex() > o2.getIndex())
                return -1;
            return 0;
        });*/
        //queue = new PriorityBlockingQueue<>(10);
        queue = new PriorityBlockingQueue<>(10, (o1, o2) -> {
            if (distance(o1.getCenter(), lockZone.tl()) > distance(o2.getCenter(), lockZone.tl()))
                return 1;
            else if (distance(o1.getCenter(), lockZone.tl()) < distance(o2.getCenter(), lockZone.tl()))
                return -1;
            return 0;
        });
        objectsToRemove = new ArrayList<>();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        boundryMat = new Mat();
        Mat image;
        Mat gray = new Mat();
        Vec3fVector circles = new Vec3fVector();
        Mat lower = new Mat(1, 1, CV_8UC4, new Scalar(160, 180, 200, 0));
        Mat upper = new Mat(1, 1, CV_8UC4, new Scalar(240, 255, 255, 0));
        Mat lowerRed = new Mat(new Scalar(0, 0, 130, 0));
        Mat upperRed = new Mat(new Scalar(100, 100, 255, 0));
        Scalar lowerScalar = new Scalar(160, 160, 160, 0);
        //Scalar lowerScalar = new Scalar(255, 255, 255, 0);
        boolean isOverlapping = false;

        try {
            grabber.start();
            lockZone = new Rect(grabber.getImageWidth() / 2 - 20, grabber.getImageHeight() / 2 - 50, 60, 60);
            edgeUp = new Rect(lockZone.x() + lockZone.width() / 2 - 5, lockZone.y() - 50, 10, 50);
            edgeDown = new Rect(lockZone.x() - 50, lockZone.y() + lockZone.height(), 80, 50);
            edgeLeft = new Rect(lockZone.x() - 50, lockZone.y() + lockZone.height() / 2 - 5, 50, 10);
            edgeRight = new Rect(lockZone.x() + lockZone.width(), lockZone.y() + lockZone.height() / 2 - 5, 50, 10);
            Frame frame;
            while ((frame = grabber.grab()) != null) {
                image = converter.convert(frame);

                inRange(image, lowerRed, upperRed, boundryMat);
                findContours(boundryMat, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

                //System.out.println(contours.size());

                cvtColor(image, gray, CV_BGR2GRAY);
                GaussianBlur(gray, gray, new Size(7, 7), 2);

                //System.out.println("Hough");
                HoughCircles(gray, circles, HOUGH_GRADIENT, 1, 20, 200, 40, 1, 40);
                //System.out.println("Hough done");
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
                    //inRange(color, lower, upper, color);
                    if (cmpScalar(mean(color), lowerScalar)) {
                        //System.out.println("Object detected");
                        DetectedObject object = new DetectedObject(roi, center, radius, color, index++);
                        if (queue.offer(object)) {
                            detectedObjects.add(object);
                            TrackerKCF tracker = TrackerKCF.create(params);
                            tracker.init(image, roi);
                            trackers.add(tracker);
                        }
                    }
                }
                //System.out.println(queue.size());
                for (DetectedObject o : detectedObjects) {
                    boolean positive = trackers.get(detectedObjects.indexOf(o)).update(image, o.getRoi());
                    o.updateCircle(image);
                    //System.out.println(trackers.size());

                    //inRange(o.getColor(), lower, upper, o.getColor());
                    if (positive && cmpScalar(mean(o.getColor()), lowerScalar)){
                        o.updateCircle(image);
                        //putText(image, (int) o.getDistance() + " cm", o.getRoi().tl(), FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 0, 0, 0));
                        if (queue.peek() == o)
                            circle(image, o.getCenter(), o.getRadius(), new Scalar(0, 0, 255, 0), 2, LINE_8, 0);
                        else
                            circle(image, o.getCenter(), o.getRadius(), new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
                    } else {
                        trackersToRemove.add(trackers.get(detectedObjects.indexOf(o)));
                        objectsToRemove.add(o);
                    }
                }

                trackers.removeAll(trackersToRemove);
                detectedObjects.removeAll(objectsToRemove);
                queue.removeAll(objectsToRemove);

                rectangle(image, lockZone, new Scalar(0, 0, 255, 0));
                rectangle(image, edgeUp, new Scalar(0, 0, 255, 0));
                rectangle(image, edgeDown, new Scalar(0, 0, 255, 0));
                rectangle(image, edgeLeft, new Scalar(0, 0, 255, 0));
                rectangle(image, edgeRight, new Scalar(0, 0, 255, 0));

                //drawContours(image, contours, -1, new Scalar(255, 0, 0, 0));

                /*if (checkBoundry(edgeUp)) {
                    //System.out.println("Boundry breached");
                    //queue.clear();
                    //detectedObjects.clear();
                    //trackers.clear();
                }*/

                /*if (checkBoundry(edgeDown)) {
                    System.out.println("Boundry breached");
                }*/


                //System.out.println(roi.x() + " " + roi.y());

                frame = converter.convert(image);
                view.showImage(frame);
                //if (queue.peek() != null)
                  //  System.out.println(queue.peek().getRoi().x());

            }
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        System.out.println("Scan stopped");
    }

    public void start() {
        SearchState searchState = SearchState.SearchForward;
        CollectState collectState = CollectState.NotFound;
        upMat = boundryMat.apply(edgeUp);
        downMat = boundryMat.apply(edgeDown);
        leftMat = boundryMat.apply(edgeLeft);
        rightMat = boundryMat.apply(edgeRight);
        int trips = 0;

        while (searchState != null) {
            //System.out.println(getTachoX());
            switch (collectState) {
                case NotFound:
                    setSpeed(500);
                    if (!queue.isEmpty() && trips < 9) {
                        collectState = collectState.nextState();
                        searchState = searchState.pauseSearch(this);
                        System.out.println(collectState);
                        System.out.println(searchState);
                    } else if (searchState == SearchState.SearchWait) {
                        searchState = searchState.nextState();
                        break;
                    } else break;
                case Found:
                    setSpeed(150);
                    if (commandX.equals("STOP") && commandY.equals("STOP")) {
                        collectState = collectState.nextState();
                    } else {
                        collectState.move(this, queue.peek());
                        break;
                    }
                case Engaged:
                    setSpeed(650);
                    collectState.move(this, queue.peek());
                    collectState = collectState.nextState();
                case Done:
                    for (DetectedObject o : queue) {
                        trackersToRemove.add(trackers.get(detectedObjects.indexOf(o)));
                        objectsToRemove.add(o);
                    }
                    //detectedObjects.clear();
                    //trackers.clear();
                    collectState = collectState.nextState();
                    searchState = searchState.nextState();
                    System.out.println(collectState);
                    System.out.println(searchState);
                    if (++trips == 9) {
                        searchState = SearchState.SearchGoal;
                    }
                    break;
            }

            switch (searchState) {
                case SearchWait:
                    break;
                case SearchReturn:
                    searchState.move(this);
                    if (commandX.equals("STOP")) {
                        searchState = searchState.nextState();
                        System.out.println("Return: " + searchState);
                    }
                    break;
                case SearchForward:
                    if (getTachoY() < 4000)
                        setSpeed(500);
                    else setSpeed(150);
                    if (checkBoundry(upMat)) {
                        System.out.println(tachoY);
                        System.out.println("Boundry breached UP");
                        trips++;
                        searchState = searchState.nextState();
                    } else {
                        searchState.move(this);
                    }
                    break;
                case SearchBackward:
                    if (getTachoY() > 1000)
                        setSpeed(500);
                    else setSpeed(150);
                    if (checkBoundry(downMat)) {
                        System.out.println(tachoY);
                        System.out.println("Boundry breached DOWN");
                        trips++;
                        searchState = searchState.nextState();
                    } else {
                        searchState.move(this);
                    }
                    break;
                case SearchLeft:
                case SearchRight:
                    setSpeed(200);
                    searchState.move(this);
                    searchState = searchState.nextState();
                    break;
                case SearchGoal:
                    setSpeed(150);
                    searchState.move(this);
                    if (Objects.equals(commandY, "STOP") && Objects.equals(commandX, "STOP")) {
                        System.out.println("Goal reached");
                        searchState = searchState.nextState();
                    }
                    break;
                case SearchDone:
                    setSpeed(800);
                    searchState.move(this);
                    System.out.println("Search done");
                    searchState = searchState.nextState();
                    break;
                default:
                    break;
            }

            /*if (trips == 3) {
                searchState = SearchState.SearchDone;
            }*/
        }
    }

    public boolean cmpScalar(Scalar s1, Scalar s2) {
        return !(s1.blue() < s2.blue()) && !(s1.green() < s2.green()) && !(s1.red() < s2.red());
    }

    public double distance(Point p1, Point p2) {
        double dist = Math.sqrt(Math.pow(p2.x() - p1.x(), 2) + Math.pow(p2.y() - p1.y(), 2));
        return dist;
    }

    public boolean checkBoundry(Mat zoneMat) {
        return (contours.size() != 0 && countNonZero(zoneMat) > 0 && (getTachoY() < 1000 || getTachoY() > 3000));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}