package mosqui2.detection;

import mosqui2.view.View;
import org.bytedeco.javacv.*;
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

public class ObjectRecog {

    public PriorityBlockingQueue<DetectedObject> getQueue() {
        return queue;
    }

    private final PropertyChangeSupport support;

    private PriorityBlockingQueue<DetectedObject> queue;
    private static Rect lockZone;
    private static Rect edgeUp;
    private static Rect edgeDown;
    private static Rect edgeDown2;
    private static Rect edgeLeft;
    private static Rect edgeRight;
    private Mat upMat, downMat, downMat2, leftMat, rightMat, lockMat;
    private Mat leftfrontBound, leftbackBound, rightfrontBound, rightbackBound, upfrontBound, upbackBound, downfrontBound, downbackBound;
    //OpenCVFrameGrabber grabber;

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
        //support.firePropertyChange("SpeedX", this.speedX, speedX);
        this.speedX = speedX;
    }

    public String getCommandX() {
        return commandX;
    }

    public String getCommandY() {
        return commandY;
    }

    private int speedX = 0;

    public int getSpeedY() {
        return speedY;
    }

    public void setSpeedY(int speedY) {
        //support.firePropertyChange("SpeedY", this.speedY, speedY);
        this.speedY = speedY;
    }

    private int speedY = 0;

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
                        }
                        else o.setCommandY("FORWARD");
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
                        o.wait(10000);
                        o.setCommandY("STOP");
                        o.setCommandX("OPEN");
                        o.wait(5000);
                        o.setTachoPoint(new java.awt.Point(o.getTachoX(), o.getTachoY() -50));
                        o.setCommandY("MOVETO");
                        o.wait(2000);
                        o.setTachoPoint(new java.awt.Point(o.getTachoX(), o.getTachoY() + 100));
                        o.setCommandY("STOP");
                        o.setCommandY("MOVETO");
                        o.setCommandX("CLOSE");
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
                System.out.println("Returning: " + o.getTachoPoint());
                o.setCommandY("STOP");
                o.setCommandX("STOP");
                o.setCommandX("MOVETO");
                o.setCommandY("MOVETO");


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

        public static SearchState previousState = SearchForward;
        public static java.awt.Point previousTacho;
        public SearchState pauseSearch(ObjectRecog o) {
            o.setTachoPoint(new java.awt.Point(o.getTachoX(), o.getTachoY()));
            o.tachoMap.put("Checkpoint", o.getTachoPoint());
            System.out.println("Paused " + o.getTachoPoint());
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
                        o.setSpeedY(150);
                        o.setCommandY("FORWARDCTRLD");
                        o.wait(2000);
                        o.setSpeedX(650);
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

        Completed {
            @Override
            public CollectState nextState() {
                return null;
            }

            @Override
            public void move(ObjectRecog o, DetectedObject object) {}
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
    //PIDController pid;

    Timer searchTimeout;

    CollectState collectState;
    SearchState searchState;

    boolean isLocked = false;

    public void setCircleParams(double[] circleParams) {
        this.circleParams = circleParams;
    }

    private double[] circleParams = {1.0, 20.0, 130.0, 41.0, 1.0, 30.0};

    HashMap<String, java.awt.Point> tachoMap;

    public java.awt.Point getTachoPoint() {
        return tachoPoint;
    }

    public void setTachoPoint(java.awt.Point tachoPoint) {
        this.tachoPoint = tachoPoint;
    }

    private java.awt.Point tachoPoint = new java.awt.Point(0, 0);

    public ObjectRecog(FrameGrabber grabber, View view) {
        this.grabber = grabber;
        this.view = view;
        tachoMap = new HashMap<>();
        //grabber = new OpenCVFrameGrabber(1);
        support = new PropertyChangeSupport(this);
        lock = new Timer(2000, e -> {
            System.out.println("LOCKED");
            isLocked = true;
        });
        unlock = new Timer(5000, e -> {
            setCommandX("TEST");
            System.out.println("UNLOCKED");
            System.out.println(queue.size());
        });
        searchTimeout = new Timer(2000, e -> {
            System.out.println("Search timeout");
            if (queue.isEmpty()) {
                collectState = CollectState.NotFound;
                searchState = SearchState.SearchReturn;
            }
        });

        searchTimeout.setRepeats(false);
        lock.setRepeats(false);
        unlock.setRepeats(false);
        //pid = new PIDController(0.1, 0.1, 0.1);
    }

    public void scan() {
        new Thread(() -> {
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
        //Scalar lowerScalar = new Scalar(0, 0, 0, 0);
        Scalar lowerScalar = new Scalar(160, 160, 160, 0);
        //Scalar lowerScalar = new Scalar(255, 255, 255, 0);
        boolean isOverlapping = false;

        try {
            grabber.stop();
            grabber.start();
            lockZone = new Rect(grabber.getImageWidth() / 2 - 20, grabber.getImageHeight() / 2 - 50, 60, 60);
            edgeUp = new Rect(lockZone.x() + 10, lockZone.y() - 40, 40, 15);
            edgeDown = new Rect(lockZone.x(), lockZone.y() + lockZone.height(), lockZone.width() , 10);
            //edgeDown2 = new Rect(lockZone.x() + lockZone.width() + 60, lockZone.y() + lockZone.height(), 10, 70);
            edgeLeft = new Rect(lockZone.x() - 40, lockZone.y() + 10, 15, 40);
            edgeRight = new Rect(lockZone.x() + lockZone.width() + 25, lockZone.y() + 10, 15, 40);
            Frame frame;
            frame = grabber.grab();
            image = converter.convert(frame);
            inRange(image, lowerRed, upperRed, boundryMat);
            lockMat = boundryMat.apply(lockZone);
            upMat = boundryMat.apply(edgeUp);
            downMat = boundryMat.apply(edgeDown);
            //downMat2 = boundryMat.apply(edgeDown2);
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
            while ((frame = grabber.grab()) != null) {
                image = converter.convert(frame);

                inRange(image, lowerRed, upperRed, boundryMat);
                findContours(boundryMat, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

                //System.out.println(contours.size());

                cvtColor(image, gray, CV_BGR2GRAY);
                GaussianBlur(gray, gray, new Size(3, 3), 2);

                //System.out.println("Hough");
                HoughCircles(gray, circles, HOUGH_GRADIENT, circleParams[0], circleParams[1], circleParams[2], circleParams[3], (int) circleParams[4], (int) circleParams[5]);
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
                        if (queue.peek() == o) {
                            circle(image, o.getCenter(), o.getRadius(), new Scalar(0, 0, 255, 0), 2, LINE_8, 0);
                            //System.out.println(o.getRadius());
                        }else {
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
                //rectangle(image, edgeDown2, new Scalar(0, 0, 255, 0));
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
        }).start();
    }

    public void start() {
        new Thread(() -> {

        searchState = SearchState.SearchForward;
        collectState = CollectState.NotFound;
        int trips = 0;

        while (searchState != null) {
            //System.out.println(getTachoX());
            switch (collectState) {
                case NotFound:
                    if (!queue.isEmpty() && trips < 10) {
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
                    if (commandX.equals("STOP") && commandY.equals("STOP")) {
                        collectState = collectState.nextState();
                        searchTimeout.stop();
                    } else {
                        DetectedObject object = queue.peek();
                        if (object != null) {
                            searchTimeout.stop();
                            //System.out.println("Object found");
                            collectState.move(this, object);
                        } else if (!searchTimeout.isRunning()) {
                            //System.out.println(searchTimeout.isRunning());
                            System.out.println("Search timeout started");
                            searchTimeout.restart();
                        }
                        break;
                    }
                case Engaged:
                    setSpeedX(650);
                    collectState.move(this, queue.peek());
                    collectState = collectState.nextState();
                case Done:
                    //detectedObjects.clear();
                    //trackers.clear();
                    collectState = collectState.nextState();
                    //searchState = searchState.nextState();
                    System.out.println(collectState);
                    System.out.println(searchState);
                    if (++trips == 10) {
                        searchState = SearchState.SearchGoal;
                        collectState = CollectState.Completed;
                    }
                    break;
                case Completed:
                    break;
            }

            switch (searchState) {
                case SearchWait:
                    break;
                case SearchReturn:
                    searchState.move(this);
                    while (tachoX < tachoMap.get("Checkpoint").x - 100 || tachoX > tachoMap.get("Checkpoint").x + 100 || tachoY < tachoMap.get("Checkpoint").y - 100 || tachoY > tachoMap.get("Checkpoint").y + 100) {
                        System.out.println("Returning...");
                    }
                    for (DetectedObject o : queue) {
                        trackersToRemove.add(trackers.get(detectedObjects.indexOf(o)));
                        objectsToRemove.add(o);
                    }
                        searchState = searchState.nextState();
                        System.out.println("Return: " + searchState);
                    break;
                case SearchForward:
                    setTachoPoint(tachoMap.get("TL"));
                    searchState.move(this);
                    if (getTachoY() < 4000) {
                        System.out.println(getSpeedY());
                        changeSpeedY(500);
                        System.out.println(getSpeedY());
                    }
                    else {
                        changeSpeedY(150);
                    }
                    if (getTachoY() == getTachoPoint().y) {
                        searchState = searchState.nextState();
                    }

                    /*switch (checkBoundry(upMat)) {

                        case -1:
                            setSpeed(100);
                            setCommandY("BACKWARD");
                            //System.out.println("Boundry breached UP");
                            //searchState = searchState.nextState();
                            break;
                        case 0:
                            setCommandY("STOP");
                            searchState = searchState.nextState();
                            break;
                        case 1:
                            setSpeed(100);
                            setCommandY("FORWARD");
                            break;
                        default:
                            searchState.move(this);
                            break;
                    }*/
                    break;
                case SearchBackward:
                    setTachoPoint(tachoMap.get("BL"));
                    searchState.move(this);
                    if (getTachoY() > 1000)
                        changeSpeedY(500);
                    else changeSpeedY(150);
                    if (getTachoY() == getTachoPoint().y) {
                        searchState = searchState.nextState();
                    }
                    //switch (checkBoundry(lockMat)) {

                        /*case 0:
                            setCommandY("STOP");
                            setCommandY("RESET");
                            searchState = searchState.nextState();
                            break;
                        case 1:
                            setSpeed(100);
                            setCommandY("BACKWARD");
                            break;
                        default:
                            searchState.move(this);
                            break;
                    }*/
                    break;
                case SearchLeft:
                case SearchRight:
                    setSpeedX(400);
                    searchState.move(this);
                    searchState = searchState.nextState();
                    break;
                case SearchGoal:
                    /*setSpeed(300);
                    System.out.println("10 balls collected!");
                    while (!commandX.equals("STOP") || !commandY.equals("STOP")) {
                        if (checkBoundry(leftMat)) {
                            setCommandX("STOP");
                        } else setCommandX("LEFT");

                        if (checkBoundry(downMat)) {
                            setCommandY("STOP");
                        } else setCommandY("BACKWARD");
                    }
                    setCommandX("RESET");
                    setCommandY("RESET");*/
                    setSpeedX(800);
                    setSpeedY(800);
                    //setTachoPoint(tachoMap.get("BL"));
                    setTachoPoint(new java.awt.Point(tachoMap.get("BR").x / 2, 1100));
                    searchState.move(this);
                    searchState = searchState.nextState();
                    break;
                case SearchDone:
                    setSpeedX(800);
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
        }).start();
    }

    public void mapField() {
        new Thread(() -> {
            //setSpeedX(300);
            int tachoX = 0;
            int tachoY = 0;

            //Mat result = new Mat();
            //findNonZero(leftMat, result);
            //System.out.println(countNonZero(leftMat.col(0)));
            //IntRawIndexer idx = result.createIndexer();
            //Point2dVector points = new Point2dVector(result);
            //System.out.println(idx.get(0, 0));
            while (!isLocked) {

                switch (checkBoundry(leftfrontBound, leftbackBound)) {
                    case -1:
                        setSpeedX(50);
                        setCommandX("RIGHT");
                        lock.restart();
                        //System.out.println("Boundry breached UP");
                        //searchState = searchState.nextState();
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
                        setSpeedX(100);
                        setCommandX("LEFT");
                        lock.restart();
                        break;
                    case -2:
                        lock.restart();
                        break;
                    default:
                        //setSpeed(300);
                        //setCommandX("LEFT");
                        break;
                }

                switch (checkBoundry(downfrontBound, downbackBound)) {
                    case -1:
                        setSpeedY(40);
                        setCommandY("FORWARD");
                        lock.restart();
                        //System.out.println("Boundry breached UP");
                        //searchState = searchState.nextState();
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
                        setSpeedY(100);
                        setCommandY("BACKWARD");
                        lock.restart();
                        break;
                    case -2:
                        lock.restart();
                        break;
                    default:
                        //setSpeed(300);
                        //setCommandX("LEFT");
                        break;
                }
            }

                /*switch (checkBoundry(lockMat)) {
                    case 0:
                        setCommandY("STOP");
                        break;
                    case 1:
                        setSpeed(100);
                        setCommandY("BACKWARD");
                        break;
                    default:
                        setSpeed(300);
                        setCommandY("BACKWARD");
                        break;
                }
            }*/
            lock.stop();
            isLocked = false;
            setCommandX("RESET");
            setCommandY("RESET");
            while (!isLocked) {
                switch (checkBoundry(rightfrontBound, rightbackBound)) {
                    case -1:
                        setSpeedX(50);
                        setCommandX("LEFT");
                        lock.restart();
                        //System.out.println("Boundry breached UP");
                        //searchState = searchState.nextState();
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
                        setSpeedX(100);
                        setCommandX("RIGHT");
                        lock.restart();
                        break;
                    case -2:
                        lock.restart();
                        break;
                    default:
                        //setSpeed(300);
                        //setCommandX("LEFT");
                        break;
                }

                switch (checkBoundry(upfrontBound, upbackBound)) {
                    case -1:
                        setSpeedY(50);
                        setCommandY("BACKWARD");
                        lock.restart();
                        //System.out.println("Boundry breached UP");
                        //searchState = searchState.nextState();
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
                        setSpeedY(100);
                        setCommandY("FORWARD");
                        lock.restart();
                        break;
                    case -2:
                        lock.restart();
                        break;
                    default:
                        //setSpeed(300);
                        //setCommandX("LEFT");
                        break;
                }
            }
            lock.stop();
            tachoX = getTachoX();
            tachoY = getTachoY();
            tachoMap.put("BL", new java.awt.Point(0, 0));
            tachoMap.put("BR", new java.awt.Point(tachoX, 0));
            tachoMap.put("TL", new java.awt.Point(0, tachoY));
            tachoMap.put("TR", new java.awt.Point(tachoX, tachoY));
            System.out.println(tachoMap);
            setSpeedX(800);
            setSpeedY(800);
            setTachoPoint(tachoMap.get("BR"));
            setCommandY("MOVETO");
            setCommandX("MOVETO");
            //setTachoPoint(tachoMap.get("BL"));
            //setCommandY("FORWARD");
            //setCommandY("BACKWARD");
            //setCommandY("FORWARD");
            //setCommandY("BACKWARD");
            //setCommandY("FORWARD");
            //setCommandY("MOVETO");
            //setCommandY("BACKWARD");
            //setCommandY("STOP");
            //setTachoPoint(new java.awt.Point(0, 2000));

            //setCommandY("MOVETO");
        }).start();
    }

    public boolean cmpScalar(Scalar s1, Scalar s2) {
        return !(s1.blue() < s2.blue()) && !(s1.green() < s2.green()) && !(s1.red() < s2.red());
    }

    public double distance(Point p1, Point p2) {
        double dist = Math.sqrt(Math.pow(p2.x() - p1.x(), 2) + Math.pow(p2.y() - p1.y(), 2));
        return dist;
    }

    public int checkBoundry(Mat frontBound, Mat backBound) {
        if ((getTachoY() < 1000 || getTachoY() > 3000) && contours.size() != 0) {
            if (countNonZero(frontBound) > 0 && countNonZero(backBound) > 0) {
                //System.out.println("STOP");
                return 0;
            } else if (countNonZero(frontBound) > 0) {
                //System.out.println("LEFT speed 100");
                return 1;
            } else if (countNonZero(backBound) > 0) {
                //System.out.println("RIGHT speed 100");
                return -1;
            } else {
                //System.out.println("LEFT speed 300");
                return 2;
            }
        } else return -2;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}