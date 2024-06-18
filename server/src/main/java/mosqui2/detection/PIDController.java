package mosqui2.detection;

public class PIDController {

        private double kp, ki, kd;
        private double integral, previousError;

        public PIDController(double kp, double ki, double kd) {
            this.kp = kp;
            this.ki = ki;
            this.kd = kd;
            this.integral = 0.0;
            this.previousError = 0.0;
        }

        public double calculate(double error) {
            integral += error;
            double derivative = error - previousError;
            previousError = error;
            return kp * error + ki * integral + kd * derivative;
        }
}
