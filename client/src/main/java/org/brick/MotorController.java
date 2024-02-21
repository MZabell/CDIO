package org.brick;
import ev3dev.actuators.Sound;
import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.sensors.Button;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class MotorController {

    private EV3LargeRegulatedMotor motorA;
    private EV3LargeRegulatedMotor motorB;
    private EV3LargeRegulatedMotor motorC;
    private EV3LargeRegulatedMotor motorD;

    public MotorController() {

        motorA = new EV3LargeRegulatedMotor(MotorPort.A);
        motorB = new EV3LargeRegulatedMotor(MotorPort.B);
        motorC = new EV3LargeRegulatedMotor(MotorPort.C);
        motorD = new EV3LargeRegulatedMotor(MotorPort.D);

        Sound sound = Sound.getInstance();
        sound.twoBeeps();

        double distanceCM = 150.0;
        int speed = 350;
        int acc = 6000;

        System.out.println("\n\n\n\n\nPress i midten to start");
        while (!Button.ENTER.isDown()) {Delay.msDelay(100);}

        sound.beep();
        moveSynchronizedDistance4Motors(distanceCM,speed,acc);
        sound.twoBeeps();
    }
    public void moveForward() {
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

        Delay.msDelay(3000);
        stop();
    }

    public void moveForwardControlled() {
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
        motorA.rotate(360, true); // Positive degrees for forward movement
        motorB.rotate(360, true); // Positive degrees for forward movement
        motorA.endSynchronization();

        // Synchronize motors C and D for backward movement
        motorC.synchronizeWith(new RegulatedMotor[]{motorD});
        motorC.startSynchronization();
        motorC.rotate(-360, true); // Negative degrees for backward movement
        motorD.rotate(-360, true); // Negative degrees for backward movement
        motorC.endSynchronization();

        // Wait for all movements to complete
        motorA.waitComplete();
        motorB.waitComplete();
        motorC.waitComplete();
        motorD.waitComplete();
    }


    public void stop() {
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



    public void moveBackward() {
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

        Delay.msDelay(3000);
        stop();
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
