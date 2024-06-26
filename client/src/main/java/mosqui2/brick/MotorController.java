package mosqui2.brick;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

// Class to provide motor control functionality to EV3 robots
public class MotorController {
    private final String mode;
    private EV3LargeRegulatedMotor motorA;
    private EV3LargeRegulatedMotor motorB;
    private EV3LargeRegulatedMotor motorC;
    private EV3LargeRegulatedMotor motorD;
    private EV3LargeRegulatedMotor motorE;

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
        } else if (mode.equals("1")) {
            motorA = new EV3LargeRegulatedMotor(MotorPort.A);
            motorB = new EV3LargeRegulatedMotor(MotorPort.B);
            motorC = new EV3LargeRegulatedMotor(MotorPort.C);
            motorD = new EV3LargeRegulatedMotor(MotorPort.D);
            motorA.resetTachoCount();
            motorB.resetTachoCount();
            motorC.resetTachoCount();
            motorD.resetTachoCount();
        }
    }

    public void moveForward(int speed) {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);

        motorA.rotate(10000, true);
        motorB.rotate(10000, true);
        motorC.rotate(-10000, true);
        motorD.rotate(-10000, true);
    }

    public void moveForwardControlled() {

        motorA.setSpeed(150);
        motorB.setSpeed(150);
        motorC.setSpeed(150);
        motorD.setSpeed(150);

        motorA.rotate(127, true);
        motorB.rotate(127, true);

        motorC.rotate(-127, true);
        motorD.rotate(-127, true);
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

        motorA.rotate(1000, true);
        motorB.rotate(1000, true);

        motorC.rotate(-1000, true);
        motorD.rotate(-1000, true);

        motorA.waitComplete();
        motorB.waitComplete();
        motorC.waitComplete();
        motorD.waitComplete();
    }


    public void stop() {
        if (mode.equals("0")) {
            motorA.hold();
            motorB.hold();
            motorE.hold();

        } else {
            //
            motorA.hold();
            motorB.hold();
            motorC.hold();
            motorD.hold();

        }
    }

    public void moveBackward(int speed) {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        motorC.setSpeed(speed);
        motorD.setSpeed(speed);

        motorA.rotate(-10000, true);
        motorB.rotate(-10000, true);
        motorC.rotate(10000, true);
        motorD.rotate(10000, true);

    }

    public void moveRight(int speed) {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);


        motorA.rotate(10000, true);
        motorB.rotate(10000, true);

    }

    public void moveLeft(int speed) {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);


        motorA.rotate(-10000, true);
        motorB.rotate(-10000, true);

    }

    public void moveLeftControlled(int speed) {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);


        motorA.rotate(-1580, true);
        motorB.rotate(-1580, true);


        motorA.waitComplete();
        motorB.waitComplete();
        System.out.println("LEFT DONE");
        openCollector();
    }

    public void moveDownControlled(int speed) {
        motorC.setSpeed(speed);

        motorC.rotate(1150, true);
        motorE.hold();

        motorC.waitComplete();
        moveUpControlled(speed);
    }

    public void moveUpControlled(int speed) {
        motorC.setSpeed(speed);

        motorC.rotateTo(0, true);

        motorC.waitComplete();
        motorC.hold();
    }

    public void openCollector() {
        motorC.setSpeed(800);

        motorC.rotateTo(1150, true);
        motorC.waitComplete();
        motorC.rotateTo(800, true);
        motorC.waitComplete();

        motorE.setSpeed(120);

        motorE.rotate(-440, true);

        motorE.waitComplete();
        motorC.setSpeed(1000);
        motorC.rotateTo(750, true);
        motorC.waitComplete();
        motorC.stop(true);
        motorC.rotateTo(800, true);
        motorC.waitComplete();
        motorC.stop(true);
        motorC.rotateTo(750, true);
        motorC.waitComplete();
        motorC.stop(true);
        motorC.rotateTo(800, true);
        motorC.waitComplete();
        motorC.stop(true);
    }

    public void closeCollector() {
        int speed = 800;
        motorE.setSpeed(speed);
        motorE.rotateTo(0, true);
        motorE.waitComplete();
        motorE.hold();

        motorC.setSpeed(800);

        motorC.rotateTo(50, true);
        motorC.waitComplete();
        motorC.hold();
    }

    public void moveTo(int speed, int x, int y) {
        motorA.setSpeed(speed);
        motorB.setSpeed(speed);
        if (mode.equals("0")) {


            motorA.rotateTo(x, true);
            motorB.rotateTo(x, true);

        } else {
            motorC.setSpeed(speed);
            motorD.setSpeed(speed);

            motorA.rotateTo(y, true);
            motorB.rotateTo(y, true);
            motorC.rotateTo(-y, true);
            motorD.rotateTo(-y, true);

        }
    }

    public void resetCollector() {
        motorE.setSpeed(120);
        motorE.rotateTo(0);
        motorC.rotateTo(0);
        motorE.hold();
        motorC.hold();
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