package mosqui2.detection;

import mosqui2.view.View;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.Vec3fVector;
import org.bytedeco.opencv.opencv_tracking.TrackerKCF;

import javax.swing.Timer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_stitching.overlapRoi;

// Class for detecting, tracking, searching, mapping and starting the robot
public class ObjectRecog {

    Thread scanThread, searchThread, mapThread;
    private static Rect lockZone;
    private static Rect edgeUp;
    private static Rect edgeDown;
    private static Rect edgeLeft;
    private static Rect edgeRight;
    private final PropertyChangeSupport support;
    Timer lock;
    FrameGrabber grabber;
    View view;
    int index = 0;
    Mat boundryMat;
    MatVector contours;
    ArrayList<DetectedObject> detectedObjects;
    ArrayList<TrackerKCF> trackers;
    ArrayList<DetectedObject> objectsToRemove;
    ArrayList<TrackerKCF> trackersToRemove;
    Timer searchTimeout;
    CollectState collectState;
    SearchState searchState;
    boolean isLocked = false;
    HashMap<String, java.awt.Point> tachoMap;
    private PriorityBlockingQueue<DetectedObject> queue;
    private Mat upMat, downMat, leftMat, rightMat, lockMat, lockMask1, lockMask2, lockCircle, whiteMat, ballMat, mask;
    private Mat leftfrontBound, leftbackBound, rightfrontBound, rightbackBound, upfrontBound, upbackBound, downfrontBound, downbackBound, lockUpbound, lockDownbound, lockLeftbound, lockRightbound;
    private boolean findOrange = true;
    private int speedX = 0;
    private int speedY = 0;
    private int tachoY = 0;
    private int tachoX = 0;
    private String commandX = "";
    private String commandY = "";
    private double[] circleParams = {1.0, 20.0, 130.0, 41.0, 1.0, 25.0};
    private java.awt.Point tachoPoint = new java.awt.Point(0, 0);

    public ObjectRecog(FrameGrabber grabber, View view) {
        this.grabber = grabber;
        this.view = view;
        tachoMap = new HashMap<>();
        support = new PropertyChangeSupport(this);
        lock = new Timer(2000, e -> {
            System.out.println("LOCKED");
            isLocked = true;
        });
        searchTimeout = new Timer(900, e -> {
            System.out.println("Search timeout");
            if (queue.isEmpty()) {
                collectState = CollectState.NotFound;
                searchState = SearchState.SearchReturn;
            }
        });

        searchTimeout.setRepeats(false);
        lock.setRepeats(false);
    }


    // ----------------------------------------------------------------------
    // Methods for changing and setting speed and commands
    // These methods uses PropertyChangeListeners as implementation of observer pattern
    // ----------------------------------------------------------------------

    public void changeSpeedX(int speedX) {
        support.firePropertyChange("SpeedX", this.speedX, speedX);
        this.speedX = speedX;
    }

    public void changeSpeedY(int speedY) {
        support.firePropertyChange("SpeedY", this.speedY, speedY);
        this.speedY = speedY;
    }

    public int getSpeedX() {
        return speedX;
    }

    public void setSpeedX(int speedX) {
        this.speedX = speedX;
    }

    public String getCommandX() {
        return commandX;
    }

    public void setCommandX(String commandX) {
        support.firePropertyChange("CommandX", this.commandX, commandX);
        this.commandX = commandX;
    }

    public String getCommandY() {
        return commandY;
    }

    public void setCommandY(String commandY) {
        support.firePropertyChange("CommandY", this.commandY, commandY);
        this.commandY = commandY;
    }

    public int getSpeedY() {
        return speedY;
    }

    public void setSpeedY(int speedY) {
        this.speedY = speedY;
    }

    // ----------------------------------------------------------------------
    // Methods for getting and setting tacho values from motors
    // ----------------------------------------------------------------------

    public int getTachoY() {
        return tachoY;
    }

    public void setTachoY(int tachoY) {
        this.tachoY = tachoY;
    }

    public int getTachoX() {
        return tachoX;
    }

    public void setTachoX(int tachoX) {
        this.tachoX = tachoX;
    }

    public void setCircleParams(double[] circleParams) {
        this.circleParams = circleParams;
    }

    public java.awt.Point getTachoPoint() {
        return tachoPoint;
    }

    public void setTachoPoint(java.awt.Point tachoPoint) {
        this.tachoPoint = tachoPoint;
    }


    // Scan method for opening camera and scanning for objects
    private void scan() {
            TrackerKCF.Params params = new TrackerKCF.Params();

            trackers = new ArrayList<>();
            trackersToRemove = new ArrayList<>();
            detectedObjects = new ArrayList<>();
            contours = new MatVector();
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
            whiteMat = new Mat();
            Mat image;
            Mat gray = new Mat();
            Mat hsv = new Mat();
            Mat orangeMat = new Mat();
            Vec3fVector circles = new Vec3fVector();
            Mat lower = new Mat(new Scalar(160, 160, 160, 0));
            Mat upper = new Mat(new Scalar(255, 255, 255, 0));
            Mat lowerRed = new Mat(new Scalar(0, 0, 130, 0));
            Mat upperRed = new Mat(new Scalar(100, 100, 255, 0));
            Mat lowerOrange = new Mat(new Scalar(10, 100, 20, 0));
            Mat upperOrange = new Mat(new Scalar(30, 255, 255, 0));
            Scalar lowerScalar = new Scalar(160, 160, 160, 0);
            boolean isOverlapping = false;
            boolean colorcmp;

            try {
                grabber.stop();
                grabber.start();
                lockZone = new Rect(grabber.getImageWidth() / 2 - 40, grabber.getImageHeight() / 2 - 50, 85, 70);
                edgeUp = new Rect(lockZone.x() + 10, lockZone.y() - 40, 35, 15);
                edgeDown = new Rect(lockZone.x(), lockZone.y() + lockZone.height(), lockZone.width(), 10);
                edgeLeft = new Rect(lockZone.x() - 40, lockZone.y() + 5, 15, 35);
                edgeRight = new Rect(lockZone.x() + lockZone.width() + 25, lockZone.y() + 5, 15, 35);
                Frame frame;
                frame = grabber.grab();
                image = converter.convert(frame);
                inRange(image, lowerRed, upperRed, boundryMat);
                inRange(image, lower, upper, whiteMat);
                ballMat = whiteMat.apply(lockZone);
                lockMat = boundryMat.apply(lockZone);
                lockCircle = new Mat();
                lockMask1 = new Mat(lockMat.size(), CV_8UC1, new Scalar(0, 0, 0, 0));
                lockMask2 = new Mat(lockMat.size(), CV_8UC1, new Scalar(0, 0, 0, 0));
                circle(lockMask1, new Point(lockMask1.cols() / 2 - 1, lockMask1.rows() / 2 - 1), Math.min(lockMask1.cols() / 2, lockMask1.rows() / 2), new Scalar(255, 255, 255, 0), -1, FILLED, 0);
                circle(lockMask2, new Point(lockMask2.cols() / 2 - 1, lockMask2.rows() / 2 - 1), (int) Math.min((lockMask2.cols() / 2.0) * 0.72, (lockMask2.rows() / 2.0) * 0.72), new Scalar(255, 255, 255, 0), -1, FILLED, 0);
                filter2D(lockMat, lockCircle, lockMat.depth(), lockMask1);
                lockUpbound = lockCircle.rowRange(0, (lockCircle.rows() - 1) / 10);
                lockDownbound = lockCircle.rowRange(lockCircle.rows() - (lockCircle.rows() - 1) / 10, lockCircle.rows() - 1);
                lockLeftbound = lockCircle.colRange(0, (lockCircle.cols() - 1) / 10);
                lockRightbound = lockCircle.colRange(lockCircle.cols() - (lockCircle.cols() - 1) / 10, lockCircle.cols() - 1);
                upMat = boundryMat.apply(edgeUp);
                downMat = boundryMat.apply(edgeDown);
                leftMat = boundryMat.apply(edgeLeft);
                rightMat = boundryMat.apply(edgeRight);
                leftfrontBound = leftMat.col(0);
                leftbackBound = leftMat.col(leftMat.cols() - 1);
                rightfrontBound = rightMat.col(rightMat.cols() - 1);
                rightbackBound = rightMat.col(0);
                upfrontBound = upMat.row(0);
                upbackBound = upMat.row(upMat.rows() - 1);
                downfrontBound = downMat.row(downMat.rows() - 1);
                downbackBound = downMat.row(0);
                while ((frame = grabber.grab()) != null && !Thread.interrupted()) {
                    image = converter.convert(frame);

                    inRange(image, lowerRed, upperRed, boundryMat);
                    inRange(image, lower, upper, whiteMat);
                    findContours(boundryMat, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

                    cvtColor(image, gray, CV_BGR2GRAY);
                    cvtColor(image, hsv, CV_BGR2HSV);


                    GaussianBlur(gray, gray, new Size(3, 3), 2);

                    HoughCircles(gray, circles, HOUGH_GRADIENT, circleParams[0], circleParams[1], circleParams[2], circleParams[3], (int) circleParams[4], (int) circleParams[5]);
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
                            if (findOrange) {
                                roiMat = new Mat(hsv, roi);
                                inRange(roiMat, lowerOrange, upperOrange, orangeMat);
                            } else
                                roiMat = new Mat(image, roi);
                        } catch (RuntimeException e) {
                            continue;
                        }

                        Mat color = new Mat(roiMat.size(), CV_8UC3, mean(roiMat));
                        colorcmp = cmpScalar(mean(color), lowerScalar);
                        if (findOrange)
                            colorcmp = countNonZero(orangeMat) > 900;
                        if (colorcmp) {
                            DetectedObject object = new DetectedObject(roi, center, radius, color, index++);
                            if (queue.offer(object)) {
                                detectedObjects.add(object);
                                TrackerKCF tracker = TrackerKCF.create(params);
                                tracker.init(image, roi);
                                trackers.add(tracker);
                            }
                        }
                    }

                    for (DetectedObject o : detectedObjects) {
                        boolean positive = trackers.get(detectedObjects.indexOf(o)).update(image, o.getRoi());
                        o.updateCircle(image);
                        if (findOrange) {
                            Mat roiMat = new Mat(hsv, o.getRoi());
                            inRange(roiMat, lowerOrange, upperOrange, orangeMat);
                            colorcmp = countNonZero(orangeMat) > 900;
                        } else {
                            colorcmp = cmpScalar(mean(o.getColor()), lowerScalar);
                        }
                        if (positive && colorcmp) {
                            o.updateCircle(image);
                            if (queue.peek() == o) {
                                circle(image, o.getCenter(), o.getRadius(), new Scalar(0, 0, 255, 0), 2, LINE_8, 0);
                            } else {
                                circle(image, o.getCenter(), o.getRadius(), new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
                            }
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

                    frame = converter.convert(image);
                    view.showImage(frame);

                }
                grabber.stop();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            System.out.println("Scan stopped");
    }

    // Start method for robot operations
    // Uses FSMs to manage states
    private void start() {
            searchState = SearchState.SearchForward;
            collectState = CollectState.NotFound;
            int trips = 0;

            while (searchState != null && !Thread.interrupted()) {
                switch (collectState) {
                    case NotFound:
                        if (!queue.isEmpty() && trips < 3) {
                            collectState = collectState.nextState();
                            if (searchState != SearchState.SearchWait)
                                searchState = searchState.pauseSearch(this);
                            System.out.println(collectState);
                            System.out.println(searchState);
                        } else if (searchState == SearchState.SearchWait) {
                            searchState = searchState.nextState();
                            break;
                        } else break;
                    case Found:
                        setSpeedX(150);
                        setSpeedY(150);
                        DetectedObject object = queue.peek();
                        collectState.move(this, object);
                        if (commandX.equals("STOP") && commandY.equals("STOP")) {
                            if (findOrange)
                                collectState = CollectState.Engaged;
                            else
                                collectState = collectState.nextState();
                            searchTimeout.stop();
                        } else {
                            collectState.move(this, object);
                            if (object != null) {
                                searchTimeout.stop();
                            } else if (!searchTimeout.isRunning()) {
                                System.out.println("Search timeout started");
                                searchTimeout.restart();
                            }
                            break;
                        }
                        break;
                    case CheckLock:
                        if (getTachoX() > 500 && getTachoX() < 2000 && getTachoY() > 900 && getTachoY() < 4000) {
                            mask = lockMask2;
                            System.out.println("Mask 2");
                        } else {
                            mask = lockMask1;
                            System.out.println("Mask 1");
                            System.out.println(getTachoX() + " " + getTachoY());
                        }
                        collectState.move(this, queue.peek());
                        if (commandX.equals("STOP") && commandY.equals("STOP")) {
                            bitwise_and(ballMat, mask, lockCircle);
                            if (!searchTimeout.isRunning())
                                searchTimeout.restart();
                            System.out.println(countNonZero(lockCircle));
                            if (countNonZero(lockCircle) > 600) {
                                collectState = collectState.nextState();
                                searchTimeout.stop();
                            }

                        } else
                            searchTimeout.stop();
                        break;
                    case Engaged:
                        setSpeedX(650);
                        collectState.move(this, queue.peek());
                        collectState = collectState.nextState();
                    case Done:
                        collectState.move(this, queue.peek());
                        if (findOrange) {
                            searchState = SearchState.SearchGoal;
                            collectState = CollectState.Reset;
                        } else {
                            collectState = collectState.nextState();
                        }
                        System.out.println(collectState);
                        System.out.println(searchState);
                        if (trips == 3) {
                            searchState = SearchState.SearchGoal;
                            collectState = CollectState.Completed;
                        }
                        break;
                    case Reset:
                        collectState.move(this, queue.peek());
                        collectState = collectState.nextState();
                        searchState = SearchState.SearchForward;
                        for (DetectedObject o : queue) {
                            trackersToRemove.add(trackers.get(detectedObjects.indexOf(o)));
                            objectsToRemove.add(o);
                        }
                        findOrange = false;
                        trips = 0;
                        break;
                    case Completed:
                        break;
                }

                switch (searchState) {
                    case SearchWait:
                        break;
                    case SearchReturn:
                        setSpeedY(500);
                        setSpeedX(500);
                        searchState.move(this);
                        while (tachoX < tachoMap.get("Checkpoint").x - 100 || tachoX > tachoMap.get("Checkpoint").x + 100 || tachoY < tachoMap.get("Checkpoint").y - 100 || tachoY > tachoMap.get("Checkpoint").y + 100) {
                            System.out.println("Returning... " + getTachoPoint());
                        }
                        for (DetectedObject o : queue) {
                            trackersToRemove.add(trackers.get(detectedObjects.indexOf(o)));
                            objectsToRemove.add(o);
                        }
                        searchState = searchState.nextState();
                        setCommandY("STOP");
                        System.out.println("Return: " + searchState);
                        break;
                    case SearchForward:
                        setTachoPoint(tachoMap.get("TL"));
                        searchState.move(this);
                        if (getTachoY() < 4500) {
                            changeSpeedY(500);
                        } else {
                            changeSpeedY(150);
                        }
                        if (getTachoY() > getTachoPoint().y - 50 && getTachoY() < getTachoPoint().y + 50) {
                            searchState = searchState.nextState();
                        }
                        break;
                    case SearchBackward:
                        setTachoPoint(tachoMap.get("BL"));
                        searchState.move(this);
                        if (getTachoY() > 500)
                            changeSpeedY(500);
                        else changeSpeedY(150);
                        if (getTachoY() < getTachoPoint().y + 50 && getTachoY() > getTachoPoint().y - 50) {
                            searchState = searchState.nextState();
                        }
                        break;
                    case SearchLeft:
                    case SearchRight:
                        setSpeedX(400);
                        searchState.move(this);
                        trips++;
                        searchState = searchState.nextState();
                        break;
                    case SearchGoal:
                        setSpeedX(800);
                        setSpeedY(800);
                        setTachoPoint(tachoMap.get("Goal"));
                        searchState.move(this);
                        searchState = searchState.nextState();
                        break;
                    case SearchDone:
                        setSpeedX(800);
                        searchState.move(this);
                        break;
                }
            }
    }

    // mapField method for mapping the boundries of the field
    // This way, the robot knows where to stop
    private void mapField() {
            int tachoX;
            int tachoY;

            while (!isLocked && !Thread.interrupted()) {

                switch (checkBoundry(leftfrontBound, leftbackBound)) {
                    case -1:
                        setSpeedX(50);
                        setCommandX("RIGHT");
                        lock.restart();
                        break;
                    case 0:
                        setCommandX("STOP");
                        break;
                    case 1:
                        setSpeedX(50);
                        setCommandX("LEFT");
                        lock.restart();
                        break;
                    case 2:
                        setSpeedX(150);
                        setCommandX("LEFT");
                        lock.restart();
                        break;
                    case -2:
                        lock.restart();
                        break;
                    default:
                        break;
                }

                switch (checkBoundry(downfrontBound, downbackBound)) {
                    case -1:
                        setSpeedY(40);
                        setCommandY("FORWARD");
                        lock.restart();
                        break;
                    case 0:
                        setCommandY("STOP");
                        break;
                    case 1:
                        setSpeedY(40);
                        setCommandY("BACKWARD");
                        lock.restart();
                        break;
                    case 2:
                        setSpeedY(150);
                        setCommandY("BACKWARD");
                        lock.restart();
                        break;
                    case -2:
                        lock.restart();
                        break;
                    default:
                        break;
                }
            }
            lock.stop();
            isLocked = false;
            setCommandX("RESET");
            setCommandY("RESET");
            while (!isLocked && !Thread.interrupted()) {
                switch (checkBoundry(rightfrontBound, rightbackBound)) {
                    case -1:
                        setSpeedX(50);
                        setCommandX("LEFT");
                        lock.restart();
                        break;
                    case 0:
                        setCommandX("STOP");
                        break;
                    case 1:
                        setSpeedX(50);
                        setCommandX("RIGHT");
                        lock.restart();
                        break;
                    case 2:
                        setCommandX("RIGHT");
                        if (getTachoX() < 2500) {
                            changeSpeedX(500);
                        } else {
                            changeSpeedX(50);
                        }
                        lock.restart();
                        break;
                    case -2:
                        lock.restart();
                        break;
                    default:
                        break;
                }

                switch (checkBoundry(upfrontBound, upbackBound)) {
                    case -1:
                        setSpeedY(50);
                        setCommandY("BACKWARD");
                        lock.restart();
                        break;
                    case 0:
                        setCommandY("STOP");
                        break;
                    case 1:
                        setSpeedY(50);
                        setCommandY("FORWARD");
                        lock.restart();
                        break;
                    case 2:
                        setCommandY("FORWARD");
                        if (getTachoY() < 4700) {
                            changeSpeedY(500);
                        } else {
                            changeSpeedY(50);
                        }
                        lock.restart();
                        break;
                    case -2:
                        lock.restart();
                        break;
                    default:
                        break;
                }
            }
            lock.stop();
            isLocked = false;
            tachoX = getTachoX();
            tachoY = getTachoY();
            tachoMap.put("BL", new java.awt.Point(0, 0));
            tachoMap.put("BR", new java.awt.Point(tachoX, 0));
            tachoMap.put("TL", new java.awt.Point(0, tachoY));
            tachoMap.put("TR", new java.awt.Point(tachoX, tachoY));
            tachoMap.put("Start", new java.awt.Point(tachoMap.get("BR").x, 0));
            tachoMap.put("Goal", new java.awt.Point(tachoMap.get("BR").x / 2, 1180));
            System.out.println(tachoMap);
            setSpeedX(800);
            setSpeedY(800);
            setTachoPoint(tachoMap.get("Start"));
            setCommandY("MOVETO");
            setCommandX("MOVETO");
    }

    public void startScan() {
        if (scanThread == null || !scanThread.isAlive()) {
            scanThread = new Thread(this::scan);
            scanThread.start();
        }
    }

    public void startSearch() {
        if (searchThread == null || !searchThread.isAlive()) {
            searchThread = new Thread(this::start);
            searchThread.start();
        }
    }

    public void startMap() {
        if (mapThread == null || !mapThread.isAlive()) {
            mapThread = new Thread(this::mapField);
            mapThread.start();
        }
    }

    public void stop() {
        if (scanThread != null) {
            scanThread.interrupt();
        }
        if (searchThread != null) {
            searchThread.interrupt();
        }
        if (mapThread != null) {
            mapThread.interrupt();
        }
        try {
            scanThread.join();
            searchThread.join();
            mapThread.join();
        } catch (Exception e) {
        }
    }

    public boolean cmpScalar(Scalar s1, Scalar s2) {
        return !(s1.blue() < s2.blue()) && !(s1.green() < s2.green()) && !(s1.red() < s2.red());
    }

    public double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x() - p1.x(), 2) + Math.pow(p2.y() - p1.y(), 2));
    }

    public int checkBoundry(Mat frontBound, Mat backBound) {
        if ((getTachoY() < 1000 || getTachoY() > 3000)) {
            if (countNonZero(frontBound) > 0 && countNonZero(backBound) > 0) {
                return 0;
            } else if (countNonZero(frontBound) > 0) {
                return 1;
            } else if (countNonZero(backBound) > 0) {
                return -1;
            } else {
                return 2;
            }
        } else return -2;
    }

    public boolean checkLock(Mat bound) {
        return countNonZero(bound) > 0;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }


    // FSM enum for searching the field
    public enum SearchState {
        SearchForward {
            @Override
            public SearchState nextState() {
                previousState = SearchForward;
                return SearchLeft;
            }

            @Override
            public void move(ObjectRecog o) {
                o.setCommandY("MOVETO");
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
                o.setCommandY("MOVETO");
                o.setCommandX("STOP");
            }
        },
        SearchLeft {
            @Override
            public SearchState nextState() {
                System.out.println(previousState);
                if (previousState == SearchForward) {
                    System.out.println("Going back");
                    previousState = SearchBackward;
                    return SearchBackward;
                } else {
                    System.out.println("Going forward");
                    previousState = SearchForward;
                    return SearchForward;
                }
            }

            @Override
            public void move(ObjectRecog o) {
                try {
                    synchronized (o) {
                        if (previousState == SearchForward) {
                            o.setCommandY("BACKWARD");
                        } else o.setCommandY("FORWARD");
                        o.wait(500);
                        o.setCommandY("STOP");
                        o.setCommandX("LEFT");
                        o.wait(3000);
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
        SearchWait {
            @Override
            public SearchState nextState() {
                return SearchReturn;
            }

            @Override
            public void move(ObjectRecog o) {
            }
        },
        SearchDone {
            @Override
            public SearchState nextState() {
                return null;
            }

            @Override
            public void move(ObjectRecog o) {
            }
        },
        SearchGoal {
            @Override
            public SearchState nextState() {
                return SearchDone;
            }

            @Override
            public void move(ObjectRecog o) {
                try {
                    synchronized (o) {
                        o.setCommandY("STOP");
                        o.setCommandX("STOP");
                        o.setCommandY("MOVETO");
                        o.setCommandX("MOVETO");
                        o.wait(8000);
                        o.setCommandY("STOP");
                        o.setCommandX("OPEN");
                        o.wait(5000);
                        o.setTachoPoint(new java.awt.Point(o.tachoMap.get("Goal").x, o.tachoMap.get("Goal").y - 150));
                        o.setCommandY("MOVETO");
                        o.wait(5000);
                        o.setTachoPoint(o.tachoMap.get("Goal"));
                        o.setCommandY("STOP");
                        o.setCommandY("MOVETO");
                        o.setCommandX("CLOSE");
                        o.wait(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },
        SearchReturn {
            @Override
            public SearchState nextState() {
                return previousState;
            }

            @Override
            public void move(ObjectRecog o) {
                o.setTachoPoint(o.tachoMap.get("Checkpoint"));
                o.setCommandY("STOP");
                o.setCommandX("STOP");
                o.setCommandX("MOVETO");
                o.setCommandY("MOVETO");
            }
        };

        public static SearchState previousState = SearchForward;

        public SearchState pauseSearch(ObjectRecog o) {
            o.setTachoPoint(new java.awt.Point(o.getTachoX(), o.getTachoY()));
            o.tachoMap.put("Checkpoint", o.getTachoPoint());
            System.out.println("Paused " + o.getTachoPoint());
            return SearchWait;
        }

        public abstract SearchState nextState();

        public abstract void move(ObjectRecog o);
    }

    // FSM enum for collecting the balls

    public enum CollectState {
        NotFound {
            @Override
            public CollectState nextState() {
                return Found;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {
            }
        },
        Found {
            @Override
            public CollectState nextState() {
                return Engaged;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {
                if (o.getTachoY() < o.tachoMap.get("TR").y || o.getTachoY() > o.tachoMap.get("BL").y) {
                    if (object != null) {
                        if (object.getRoi().y() < lockZone.y()) {
                            o.setCommandY("FORWARD");
                        } else if (object.getRoi().y() + object.getRoi().height() > lockZone.y() + lockZone.height()) {
                            o.setCommandY("BACKWARD");
                        } else {
                            o.setCommandY("STOP");
                        }
                    }
                } else {
                    o.setCommandY("STOP");
                }

                if (o.getTachoX() < o.tachoMap.get("TR").x || o.getTachoX() > o.tachoMap.get("BL").x) {
                    if (object != null) {
                        if (object.getRoi().x() < lockZone.x()) {
                            o.setCommandX("LEFT");
                        } else if (object.getRoi().x() + object.getRoi().width() > lockZone.x() + lockZone.width()) {
                            o.setCommandX("RIGHT");
                        } else {
                            o.setCommandX("STOP");
                        }
                    }
                } else {
                    o.setCommandX("STOP");
                }
            }
        },

        CheckLock {
            @Override
            public CollectState nextState() {
                return Engaged;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {
                while (o.commandX.equals("STOP") && o.commandY.equals("STOP")) {
                    bitwise_and(o.lockMat, o.mask, o.lockCircle);
                    if (o.checkLock(o.lockLeftbound)) {
                        System.out.println("Leftbound breached");
                        o.setSpeedX(50);
                        o.setCommandX("RIGHT");
                    } else if (o.checkLock(o.lockRightbound)) {
                        System.out.println("Rightbound breached");
                        o.setSpeedX(50);
                        o.setCommandX("LEFT");
                    } else o.setCommandX("STOP");

                    if (o.checkLock(o.lockUpbound)) {
                        System.out.println("Upbound breached");
                        o.setSpeedY(40);
                        o.setCommandY("BACKWARD");
                    } else if (o.checkLock(o.lockDownbound)) {
                        System.out.println("Downbound breached");
                        o.setSpeedY(40);
                        o.setCommandY("FORWARD");
                    } else o.setCommandY("STOP");
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
                        o.setSpeedY(150);
                        o.setCommandY("FORWARDCTRLD");
                        o.wait(1000);
                        o.setSpeedX(650);
                        o.setCommandX("DOWN");
                        o.wait(4000);
                    }
                } catch (InterruptedException e) {
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
            public void move(ObjectRecog o, DetectedObject object) {
                o.setCommandX("RESETCOL");
            }
        },

        Completed {
            @Override
            public CollectState nextState() {
                return null;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {
            }
        },

        Reset {
            @Override
            public CollectState nextState() {
                return NotFound;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {
                try {
                    synchronized (o) {
                        o.setTachoPoint(o.tachoMap.get("Start"));
                        o.setSpeedX(800);
                        o.setCommandX("MOVETO");
                        o.changeSpeedY(801);
                        o.setCommandY("MOVETO");
                        o.wait(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        public abstract CollectState nextState();

        public abstract void move(ObjectRecog o, DetectedObject object);
    }
}