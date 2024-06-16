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
    String[] splitMessage;
    int speed;

    Thread tachoThread;

    MotorController motorController;

    String mode;

    public Client(String IP) {
        try {
            socket = new Socket(IP, 5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
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
            motorController.stop();
            System.out.println("Client disconnected");
        }
    }

    private void modeX() throws IOException {
        out.writeUTF("READY");
        while (!(message = in.readUTF()).equals("EXIT")) {
            //out.writeUTF(String.valueOf(motorController.getTacho()));
            splitMessage = message.split(":");
            speed = Integer.parseInt(splitMessage[1]);
            System.out.println("Command: " + splitMessage[0] + " --- Speed: " + splitMessage[1]);
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
                case "GETTACHO":
                    out.writeUTF(String.valueOf(motorController.getTacho()));
                    System.out.println("Sent tacho");
                    break;
                default:
                    motorController.stop();
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
            System.out.println("Command: " + splitMessage[0] + " --- Speed: " + splitMessage[1]);
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
                case "GETTACHO":
                    out.writeUTF(String.valueOf(motorController.getTacho()));
                    break;
                default:
                    motorController.stop();
                    break;
            }
        }
    }

    private void sendTacho() {
        while (true) {
            try {
                out.writeUTF(String.valueOf(motorController.getTacho()));
                //System.out.println("Sent tacho: " + motorController.getTacho());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
