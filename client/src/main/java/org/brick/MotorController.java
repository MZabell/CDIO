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
    public MotorController(String mode) {

        this.mode = mode;

        if (mode.equals("0")) {
            motorA = new EV3LargeRegulatedMotor(MotorPort.A);
            motorB = new EV3LargeRegulatedMotor(MotorPort.B);
            motorC = new EV3LargeRegulatedMotor(MotorPort.C);
            motorE = new EV3LargeRegulatedMotor(MotorPort.D);
            motorE.setSpeed(speed);
            motorE.setAcceleration(acc);
        } else if (mode.equals("1")) {
            motorA = new EV3LargeRegulatedMotor(MotorPort.A);
            motorB = new EV3LargeRegulatedMotor(MotorPort.B);
            motorC = new EV3LargeRegulatedMotor(MotorPort.C);
            motorD = new EV3LargeRegulatedMotor(MotorPort.D);
            motorD.setSpeed(speed);
            motorD.setAcceleration(acc);
        }

        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);

        motorA.setAcceleration(acc);
        motorB.setAcceleration(acc);
        motorC.setAcceleration(acc);
    }
    public void moveForward() {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);
        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.forward();
        motorB.forward();
        motorA.endSynchronization();

        motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        motorC.startSynchronization();
        motorC.backward();
        motorD.backward();
        motorC.endSynchronization();
    }
    public void moveForwardControlled() {

        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);

        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(100, true);
        motorB.rotate(100, true);
        motorA.endSynchronization();

        // Synchronize motors C and D for backward movement
        motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        motorC.startSynchronization();
        motorC.rotate(-100, true);
        motorD.rotate(-100, true);
        motorC.endSynchronization();

        System.out.println(motorA.isStalled());
        System.out.println(motorB.isStalled());
        System.out.println(motorC.isStalled());
        System.out.println(motorD.isStalled());

        motorA.waitComplete();
        //System.out.println("FORWARD DONE1");
        motorB.waitComplete();
        //System.out.println("FORWARD DONE2");
        motorC.waitComplete();
        //System.out.println("FORWARD DONE3");
        motorD.waitComplete();
        //System.out.println("FORWARD DONE4");
    }

    public void moveForwardControlled2() {

        motorA.setSpeed(speed + 650);
        motorB.setSpeed(speed + 650);
        motorC.setSpeed(speed + 650);
        motorD.setSpeed(speed + 650);

        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(950, true);
        motorB.rotate(950, true);
        motorA.endSynchronization();

        // Synchronize motors C and D for backward movement
        motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        motorC.startSynchronization();
        motorC.rotate(-950, true);
        motorD.rotate(-950, true);
        motorC.endSynchronization();

        System.out.println(motorA.isStalled());
        System.out.println(motorB.isStalled());
        System.out.println(motorC.isStalled());
        System.out.println(motorD.isStalled());

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

            motorA.resetTachoCount();
            motorB.resetTachoCount();
            motorC.resetTachoCount();
            motorE.resetTachoCount();
        } else {
            motorA.synchronizeWith(new RegulatedMotor[]{motorB});
            motorA.startSynchronization();
            motorA.stop(true);
            motorB.stop(true);
            motorA.endSynchronization();

            motorC.synchronizeWith(new RegulatedMotor[]{motorD});
            motorC.startSynchronization();
            motorC.stop(true);
            motorD.stop(true);
            motorC.endSynchronization();

            motorA.resetTachoCount();
            motorB.resetTachoCount();
            motorC.resetTachoCount();
            motorD.resetTachoCount();
        }
    }
    public void moveBackward() {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);
        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.backward();
        motorB.backward();
        motorA.endSynchronization();

        motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        motorC.startSynchronization();
        motorC.forward();
        motorD.forward();
        motorC.endSynchronization();
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
    public void moveRight(){
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.forward();
        motorB.forward();
        motorA.endSynchronization();
    } // right == backward
    public void moveLeft(){
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.backward();
        motorB.backward();
        motorA.endSynchronization();
    } //left == forward

    public void moveLeftControlled() {
        //motorA.setSpeed(speed + 500);
        motorA.setSpeed(speed + 650);
        motorB.setSpeed(speed + 650);
        motorA.synchronizeWith(new RegulatedMotor[]{motorB});
        motorA.startSynchronization();
        motorA.rotate(-1580, true);
        motorB.rotate(-1580, true);
        motorA.endSynchronization();

        System.out.println(motorA.isStalled());
        System.out.println(motorB.isStalled());

        motorA.waitComplete();
        motorB.waitComplete();
        System.out.println("LEFT DONE");
        openCollector();
        //moveRight();
    }

    public void moveDown() {
        motorC.forward();
    }

    public void moveDownControlled() {
        motorC.setSpeed(speed + 500);

        motorC.rotate(1150, true);

        System.out.println(motorC.isStalled());

        motorC.waitComplete();
        moveUpControlled();
        //moveUp();
    }

    public void moveUpControlled() {
        motorC.setSpeed(speed + 500);

        motorC.rotate(-1080, true);

        System.out.println(motorC.isStalled());

        motorC.waitComplete();
        //moveUp();
    }

    public void moveUp() {
        motorC.setSpeed(1);
        motorC.backward();
    }

    public void openCollector() {
        motorC.setSpeed(speed + 650);

        motorC.rotate(700, true);
        motorC.waitComplete();

        motorE.setSpeed(speed + 250);

        motorE.rotate(-400, true);

        System.out.println(motorE.isStalled());

        motorE.waitComplete();
        //motorE.backward();
    }

    public void closeCollector() {
        motorE.setSpeed(speed + 250);

        motorE.rotate(386, true);

        System.out.println(motorE.isStalled());

        motorE.waitComplete();

        motorC.setSpeed(speed + 650);

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
}