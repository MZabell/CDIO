package mosqui2.brick;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class PIDController {
    private double kp; // Proportional gain
    private double ki; // Integral gain
    private double kd; // Derivative gain

    private double integral;
    private double previousError;

    public PIDController(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.integral = 0;
        this.previousError = 0;

        EV3LargeRegulatedMotor motorA = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3LargeRegulatedMotor motorB = new EV3LargeRegulatedMotor(MotorPort.B);
        EV3LargeRegulatedMotor motorC = new EV3LargeRegulatedMotor(MotorPort.C);
        EV3LargeRegulatedMotor motorD = new EV3LargeRegulatedMotor(MotorPort.D);
        //PIDController pidController = new PIDController(0.1, 0.1, 0.1);
        //double actual;
        double actual = 0;
        double setpoint = 5000;
        double minSpeed = 100;
        double speed = 1000;
        while (actual < setpoint) {
            long startTime = System.currentTimeMillis();
            actual = (motorA.getTachoCount() + motorB.getTachoCount() - motorC.getTachoCount() - motorD.getTachoCount()) / 4.0;
            //speed = pidController.calculate(setpoint, actual);
            speed = Math.sin(Math.PI * actual / setpoint) * (1000 - minSpeed) + minSpeed;
            //if (actual == 0) speed = 50;
            //if (speed > 1000) speed = 1000;
            motorA.setSpeed((int) speed);
            motorB.setSpeed((int) speed);
            motorC.setSpeed((int) speed);
            motorD.setSpeed((int) speed);
            motorA.rotateTo((int) setpoint, true);
            motorB.rotateTo((int) setpoint, true);
            motorC.rotateTo(-(int) setpoint, true);
            motorD.rotateTo(-(int) setpoint, true);
            System.out.println("Speed: " + speed + "Actual: " + actual);
            //actual += 200;
            long timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed < (int) (1f / 10 * 1000) - timeElapsed) {
                //Thread.sleep((int) (1f / 10 * 1000) - timeElapsed);
            }
        }
    }

    public double calculate(double setpoint, double actual) {
        double error = setpoint - actual;
        integral += error;
        double derivative = error - previousError;
        previousError = error;
        return (kp * error + ki * integral + kd * derivative) / 2;
    }
}