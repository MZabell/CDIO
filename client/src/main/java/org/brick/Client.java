package org.brick;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    String message;

    MotorController motorController;

    String mode;

    public Client(String IP) {
        try {
            socket = new Socket(IP, 5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            mode = in.readUTF();
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
            motorController.stop();
            throw new RuntimeException(e);
        }
    }

    private void modeX() throws IOException {
        while (!(message = in.readUTF()).equals("EXIT")) {
            System.out.println("Received message: " + message);
            switch (message) {
                case "LEFT":
                    motorController.moveLeft();
                    break;
                case "RIGHT":
                    motorController.moveRight();
                    break;
                case "DOWN":
                    motorController.moveDownControlled();
                    break;
                case "UP":
                    motorController.moveUpControlled();
                    break;
                case "OPEN":
                    motorController.openCollector();
                    break;
                case "CLOSE":
                    motorController.closeCollector();
                    break;
                case "LEFTCTRLD":
                    motorController.moveLeftControlled();
                    break;
                default:
                    motorController.stop();
                    break;
            }
        }
    }

    private void modeY() throws IOException {
        while (!(message = in.readUTF()).equals("EXIT")) {
            System.out.println("Received message: " + message);
            switch (message) {
                case "FORWARD":
                    motorController.moveForward();
                    break;
                case "BACKWARD":
                    motorController.moveBackward();
                    break;
                case "FORWARDCTRLD":
                    motorController.moveForwardControlled();
                    break;
                case "FORWARDCTRLD2":
                    motorController.moveForwardControlled2();
                    break;
                default:
                    motorController.stop();
                    break;
            }
        }
    }
}
