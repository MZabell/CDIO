package mosqui2.brick;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) {
        Client client = new Client(args[0]);
        System.exit(0);
    }
}
