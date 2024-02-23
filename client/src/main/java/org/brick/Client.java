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
            motorController = new MotorController();
            socket = new Socket(IP, 5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            mode = in.readUTF();

            switch (mode) {
                case "0":
                    modeX();
                    break;
                case "1":
                    modeY();
                    break;
            }

            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
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
                default:
                    motorController.stop();
                    break;
            }
        }
    }
}
