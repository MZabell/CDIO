package org.brick;

import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class MotorController {

    private final EV3MediumRegulatedMotor motor;

    public MotorController() {
        motor = new EV3MediumRegulatedMotor(MotorPort.A);

        motor.setSpeed(500);
    }

    public void moveForward() {
        motor.forward();
    }

    public void moveBackward() {
        motor.backward();
    }

    public void stop() {
        motor.stop();
    }
}
