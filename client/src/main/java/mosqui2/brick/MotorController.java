package mosqui2.brick;
import ev3dev.actuators.Sound;
import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import ev3dev.actuators.lego.motors.Motor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

import static java.lang.Thread.sleep;

public class MotorController {
    private EV3LargeRegulatedMotor motorA;
    private EV3LargeRegulatedMotor motorB;
    private EV3LargeRegulatedMotor motorC;
    private EV3LargeRegulatedMotor motorD;
    private EV3LargeRegulatedMotor motorE;

    private String mode;

    static final int speed = 150;
    static final int acc = 4000;

    private RegulatedMotor[] motors;
    public MotorController(String mode) throws InterruptedException {

        this.mode = mode;


        if (mode.equals("0")) {
            motorA = new EV3LargeRegulatedMotor(MotorPort.A);
            motorB = new EV3LargeRegulatedMotor(MotorPort.B);
            motorC = new EV3LargeRegulatedMotor(MotorPort.C);
            motorE = new EV3LargeRegulatedMotor(MotorPort.D);
            motorA.resetTachoCount();
            motorB.resetTachoCount();
            motorC.resetTachoCount();
            motorE.resetTachoCount();
            //motorE.setSpeed(speed);
            //motorE.setAcceleration(acc);
        } else if (mode.equals("1")) {
            motorA = new EV3LargeRegulatedMotor(MotorPort.A);
            motorB = new EV3LargeRegulatedMotor(MotorPort.B);
            motorC = new EV3LargeRegulatedMotor(MotorPort.C);
            motorD = new EV3LargeRegulatedMotor(MotorPort.D);
            motorA.resetTachoCount();
            motorB.resetTachoCount();
            motorC.resetTachoCount();
            motorD.resetTachoCount();
            /*motorA.setAcceleration(acc);
            motorB.setAcceleration(acc);
            motorC.setAcceleration(acc);
            motorD.setAcceleration(acc);*/
            motors = new RegulatedMotor[]{motorB, motorC, motorD};
            motorA.synchronizeWith(motors);
        }
    }
    public void moveForward(int speed) {
        //System.out.println(motorA.getTachoCount());
        //System.out.println("MotorA: " + motorA.getTachoCount() + " MotorB: " + motorB.getTachoCount() + " MotorC: " + motorC.getTachoCount() + " MotorD: " + motorD.getTachoCount());
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);

        //motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(10000, true);
        motorB.rotate(10000, true);
        motorC.rotate(-10000, true);
        motorD.rotate(-10000, true);
        motorA.endSynchronization();
        /*System.out.println(motorA.getPosition());
        System.out.println(motorB.getPosition());
        System.out.println(motorC.getPosition());
        System.out.println(motorD.getPosition());*/

        //System.out.println(motorA.isStalled());
        //System.out.println(motorB.isStalled());
        //System.out.println(motorC.isStalled());
        //System.out.println(motorD.isStalled());
        //System.out.println("MotorA: " + motorA.getTachoCount() + " MotorB: " + motorB.getTachoCount() + " MotorC: " + motorC.getTachoCount() + " MotorD: " + motorD.getTachoCount());
    }
    public void moveForwardControlled() {

        motorA.setSpeed(150);
        motorB.setSpeed(150);
        motorC.setSpeed(150);
        motorD.setSpeed(150);

        //motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(100, true);
        motorB.rotate(100, true);
        // Synchronize motors C and D for backward movement
        //motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        motorC.rotate(-100, true);
        motorD.rotate(-100, true);
        motorA.endSynchronization();
        motorA.waitComplete();
        motorB.waitComplete();
        motorC.waitComplete();
        motorD.waitComplete();
    }

    public void moveForwardControlled2() {

        motorA.setSpeed(800);
        motorB.setSpeed(800);
        motorC.setSpeed(800);
        motorD.setSpeed(800);

        //motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(1000, true);
        motorB.rotate(1000, true);

        // Synchronize motors C and D for backward movement
        //motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        //motorC.startSynchronization();
        motorC.rotate(-1000, true);
        motorD.rotate(-1000, true);
        motorA.endSynchronization();

        motorA.waitComplete();
        //System.out.println("FORWARD DONE1");
        motorB.waitComplete();
        //System.out.println("FORWARD DONE2");
        motorC.waitComplete();
        //System.out.println("FORWARD DONE3");
        motorD.waitComplete();
        //System.out.println("FORWARD DONE4");
    }


    public void stop() {
        if (mode.equals("0")) {
            motorA.synchronizeWith(new RegulatedMotor[]{motorB});
            motorA.startSynchronization();
            motorA.hold();
            motorB.hold();
            motorA.endSynchronization();

            motorC.synchronizeWith(new RegulatedMotor[]{motorE});
            motorC.startSynchronization();
            motorC.stop(true);
            motorE.stop(true);
            motorC.endSynchronization();

        } else {
            //motorA.synchronizeWith(new RegulatedMotor[]{motorB});
            motorA.startSynchronization();
            motorA.hold();
            motorB.hold();
            motorC.hold();
            motorD.hold();
            motorA.endSynchronization();
        }
    }
    public void moveBackward(int speed) {
        //System.out.println(motorA.getTachoCount());
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);
        //motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(-10000, true);
        motorB.rotate(-10000, true);
        motorC.rotate(10000, true);
        motorD.rotate(10000, true);
        motorA.endSynchronization();
    }
    public void moveBackwardControlled() {

        motorA.setSpeed(300);
        motorB.setSpeed(300);
        motorC.setSpeed(300);
        motorD.setSpeed(300);

        motorA.setAcceleration(3000);
        motorB.setAcceleration(3000);
        motorC.setAcceleration(3000);
        motorD.setAcceleration(3000);

        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(-360, true);
        motorB.rotate(-360, true);
        motorA.endSynchronization();

        motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        motorC.startSynchronization();
        motorC.rotate(360, true);
        motorD.rotate(360, true);
        motorC.endSynchronization();

        motorA.waitComplete();
        motorB.waitComplete();
        motorC.waitComplete();
        motorD.waitComplete();
    }
    public void moveRight(int speed){
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(10000, true);
        motorB.rotate(10000, true);
        motorA.endSynchronization();
    } // right == backward
    public void moveLeft(int speed){
            motorA.setSpeed(speed);
            motorB.setSpeed(speed);
            motorA.synchronizeWith(new RegulatedMotor[]{motorB});
            motorA.startSynchronization();
            motorA.rotate(-10000, true);
            motorB.rotate(-10000, true);
            motorA.endSynchronization();
    } //left == forward

    public void moveLeftControlled(int speed) {
        //motorA.setSpeed(speed + 500);
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(-1580, true);
        motorB.rotate(-1580, true);
        motorA.endSynchronization();


        motorA.waitComplete();
        motorB.waitComplete();
        System.out.println("LEFT DONE");
        openCollector();
        //moveRight();
    }

    public void moveDown() {
        motorC.forward();
    }

    public void moveDownControlled(int speed) {
        motorC.setSpeed(speed);

        motorC.rotate(1150, true);

        motorC.waitComplete();
        moveUpControlled(speed);
        //moveUp();
    }

    public void moveUpControlled(int speed) {
        motorC.setSpeed(speed);

        motorC.rotateTo(0, true);
        //motorC.rotate(-1080, true);

        motorC.waitComplete();
        motorC.hold();
        //moveUp();
    }

    public void moveUp() {
        motorC.setSpeed(1);
        motorC.backward();
    }

    public void openCollector() {
        motorC.setSpeed(800);

        motorC.rotate(700, true);
        motorC.waitComplete();

        motorE.setSpeed(300);

        motorE.rotate(-400, true);

        motorE.waitComplete();
        motorC.setSpeed(500);
        motorC.rotate(-300);
        motorC.waitComplete();
        motorC.rotate(300);
        motorC.waitComplete();
        //motorE.backward();
    }

    public void closeCollector() {
        int speed = 800;
        /*do {
            motorE.stop();
            motorE.setSpeed(speed);
            motorE.rotateTo(0, true);
            System.out.println(motorE.isMoving() + " " + speed);
            speed += 10;
        } while (motorE.isMoving());*/

        motorE.setSpeed(speed);
        motorE.rotateTo(0, true);
        motorE.waitComplete();
        motorE.hold();

        motorC.setSpeed(800);

        motorC.rotateTo(0);
        motorC.waitComplete();
        motorC.hold();
    }
    public void moveSynchronizedDistance4Motors(double distanceInCm, int speed, int acc) {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);

        motorA.setAcceleration(acc);
        motorB.setAcceleration(acc);
        motorC.setAcceleration(acc);
        motorD.setAcceleration(acc);

        double wheelDiameterInCm = 4.06;
        double wheelCircumferenceInCm = Math.PI * wheelDiameterInCm;
        double rotationsNeeded = distanceInCm / wheelCircumferenceInCm;
        int degreesToRotate = (int) (rotationsNeeded * 360);

        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorC.synchronizeWith(new RegulatedMotor[]{motorD});

        motorA.startSynchronization();
        motorA.rotate(degreesToRotate, true);
        motorB.rotate(degreesToRotate, true);
        motorA.endSynchronization();

        motorC.startSynchronization();
        motorC.rotate(-degreesToRotate, true);
        motorD.rotate(-degreesToRotate, true);
        motorC.endSynchronization();

        motorA.waitComplete();
        motorB.waitComplete();
        motorC.waitComplete();
        motorD.waitComplete();
    }

    public void moveTo(int speed, int x, int y) {
        //System.out.println("MotorA: " + motorA.getTachoCount() + " MotorB: " + motorB.getTachoCount() + " MotorC: " + motorC.getTachoCount() + " MotorD: " + motorD.getTachoCount());
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        if (mode.equals("0")) {
            motorA.synchronizeWith(new RegulatedMotor[]{motorB});
            motorA.startSynchronization();
            motorA.rotateTo(x, true);
            motorB.rotateTo(x, true);
            motorA.endSynchronization();
        } else {
            motorC.setSpeed(speed);
            motorD.setSpeed(speed);
            motorA.startSynchronization();
            motorA.rotateTo(y, true);
            motorB.rotateTo(y, true);
            motorC.rotateTo(-y, true);
            motorD.rotateTo(-y, true);
            motorA.endSynchronization();
        }
        /*motorA.waitComplete();
        motorB.waitComplete();
        motorA.hold();
        motorB.hold();
        if (mode.equals("1")) {
            motorC.hold();
            motorD.hold();
        }*/
        //motorA.waitComplete();
        //motorB.waitComplete();
        //System.out.println("MotorA: " + motorA.getTachoCount() + " MotorB: " + motorB.getTachoCount() + " MotorC: " + motorC.getTachoCount() + " MotorD: " + motorD.getTachoCount());
    }

    public void exit() {
        motorA.stop(true);
        motorB.stop(true);
        motorC.stop(true);
        if (mode.equals("0")) {
            motorE.stop(true);
        } else {
            motorD.stop(true);
        }
    }

    public void resetTacho() {
        motorA.resetTachoCount();
        motorB.resetTachoCount();
        motorC.resetTachoCount();
        if (mode.equals("0")) {
            motorE.resetTachoCount();
        } else {
            motorD.resetTachoCount();
        }
    }

    public int getTacho() {
        return motorA.getTachoCount();
    }
}