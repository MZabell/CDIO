package mosqui2.brick;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class Client {

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    String message;
    String[] splitMessage;
    int speed;

    Thread tachoThread;

    MotorController motorController;

    String mode;

    private ScheduledExecutorService executor;

    private StreamHandler logger;

    public Client(String IP) {
        try {
            executor = Executors.newScheduledThreadPool(1);
            socket = new Socket(IP, 5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            //logger = new StreamHandler(socket.getOutputStream(), new SimpleFormatter());
            tachoThread = new Thread(this::sendTacho);
            mode = in.readUTF();
            //mode = "1";
            motorController = new MotorController(mode);

            switch (mode) {
                case "0":
                    System.out.println("Mode X");
                    modeX();
                    break;
                case "1":
                    System.out.println("Mode Y");
                    modeY();
                    break;
            }

            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
            motorController.exit();
            System.out.println("Client disconnected");
        }
    }

    private void modeX() throws IOException {
        out.writeUTF("READY");
        while (!(message = in.readUTF()).equals("EXIT")) {
            //out.writeUTF(String.valueOf(motorController.getTacho()));
            splitMessage = message.split(":");
            speed = Integer.parseInt(splitMessage[1]);
            System.out.println("Command: " + splitMessage[0] + " --- Speed: " + speed);
            switch (splitMessage[0]) {
                case "START":
                    tachoThread.start();
                    break;
                case "LEFT":
                    motorController.moveLeft(speed);
                    break;
                case "RIGHT":
                    motorController.moveRight(speed);
                    break;
                case "DOWN":
                    motorController.moveDownControlled(speed);
                    break;
                case "UP":
                    motorController.moveUpControlled(speed);
                    break;
                case "OPEN":
                    motorController.openCollector();
                    break;
                case "CLOSE":
                    motorController.closeCollector();
                    break;
                case "LEFTCTRLD":
                    motorController.moveLeftControlled(speed);
                    break;
                case "RESET":
                    motorController.resetTacho();
                    break;
                case "MOVETO":
                    motorController.moveTo(speed, Integer.parseInt(splitMessage[2]), Integer.parseInt(splitMessage[3]));
                    break;
                case "EXIT":
                    motorController.exit();
                    break;
                case "STOP":
                    motorController.stop();
                    break;
                default:
                    motorController.exit();
                    break;
            }
        }
    }

    private void modeY() throws IOException {
        /*while (mode.equals("1")) {
            motorController.getTacho();
            motorController.moveForwardControlled();
        }*/
        //System.exit(0);
        out.writeUTF("READY");
        while (!(message = in.readUTF()).equals("EXIT")) {
            //out.writeUTF(String.valueOf(motorController.getTacho()));
            splitMessage = message.split(":");
            speed = Integer.parseInt(splitMessage[1]);
            System.out.println("Command: " + splitMessage[0] + " --- Speed: " + speed);
            switch (splitMessage[0]) {
                case "START":
                    tachoThread.start();
                    break;
                case "FORWARD":
                    motorController.moveForward(speed);
                    break;
                case "BACKWARD":
                    motorController.moveBackward(speed);
                    break;
                case "FORWARDCTRLD":
                    motorController.moveForwardControlled();
                    break;
                case "FORWARDCTRLD2":
                    motorController.moveForwardControlled2();
                    break;
                case "RESET":
                    motorController.resetTacho();
                    break;
                case "MOVETO":
                    motorController.moveTo(speed, Integer.parseInt(splitMessage[2]), Integer.parseInt(splitMessage[3]));
                    break;
                case "EXIT":
                    motorController.exit();
                    break;
                case "STOP":
                    motorController.stop();
                    break;
                default:
                    motorController.exit();
                    break;
            }
        }
    }

    private void sendTacho() {
        executor.scheduleAtFixedRate(() -> {
            try {
                out.writeUTF(String.valueOf(motorController.getTacho()));
                //System.out.println("Sent tacho: " + motorController.getTacho());
            } catch (IOException e) {
                motorController.exit();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
}
