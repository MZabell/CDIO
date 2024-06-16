package org.brick;
import ev3dev.actuators.Sound;
import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;

public class MotorController {
    private EV3LargeRegulatedMotor motorA;
    private EV3LargeRegulatedMotor motorB;
    private EV3LargeRegulatedMotor motorC;
    private EV3LargeRegulatedMotor motorD;
    private EV3LargeRegulatedMotor motorE;

    private String mode;

    static final int speed = 150;
    static final int acc = 10;

    private RegulatedMotor[] motors;
    public MotorController(String mode) {

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
            motorE.setSpeed(speed);
            motorE.setAcceleration(acc);
        } else if (mode.equals("1")) {
            motorA = new EV3LargeRegulatedMotor(MotorPort.A);
            motorB = new EV3LargeRegulatedMotor(MotorPort.B);
            motorC = new EV3LargeRegulatedMotor(MotorPort.C);
            motorD = new EV3LargeRegulatedMotor(MotorPort.D);
            motorA.resetTachoCount();
            motorB.resetTachoCount();
            motorC.resetTachoCount();
            motorD.resetTachoCount();
            motors = new RegulatedMotor[]{motorB, motorC, motorD};
            motorA.synchronizeWith(motors);
        }
    }
    public void moveForward(int speed) {
        //System.out.println(motorA.getTachoCount());
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);
        //motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.forward();
        motorB.forward();
        motorC.backward();
        motorD.backward();
        motorA.endSynchronization();
    }
    public void moveForwardControlled() {

        motorA.setSpeed(150);
        motorB.setSpeed(150);
        motorC.setSpeed(150);
        motorD.setSpeed(150);

        //motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.forward();
        motorB.forward();
        motorA.rotate(100, true);
        motorB.rotate(100, true);
        // Synchronize motors C and D for backward movement
        //motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        motorC.backward();
        motorD.backward();
        motorC.rotate(100, true);
        motorD.rotate(100, true);
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
            motorA.stop(true);
            motorB.stop(true);
            motorA.endSynchronization();

            motorC.synchronizeWith(new RegulatedMotor[]{motorE});
            motorC.startSynchronization();
            motorC.stop(true);
            motorE.stop(true);
            motorC.endSynchronization();

        } else {
            //motorA.synchronizeWith(new RegulatedMotor[]{motorB});
            motorA.startSynchronization();
            motorA.stop(true);
            motorB.stop(true);
            motorC.stop(true);
            motorD.stop(true);
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
        motorA.backward();
        motorB.backward();
        motorC.forward();
        motorD.forward();
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
        motorA.forward();
        motorB.forward();
        motorA.endSynchronization();
    } // right == backward
    public void moveLeft(int speed){
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.backward();
        motorB.backward();
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

        motorC.rotate(-1080, true);

        motorC.waitComplete();
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

        motorE.setSpeed(400);

        motorE.rotate(-400, true);

        motorE.waitComplete();
        //motorE.backward();
    }

    public void closeCollector() {
        motorE.setSpeed(400);

        motorE.rotate(386, true);

        motorE.waitComplete();

        motorC.setSpeed(800);

        motorC.rotate(-630, true);
        motorC.waitComplete();
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

    public int getTacho() {
        //System.out.println(motorA.getTachoCount());
        return motorA.getTachoCount();
    }
}