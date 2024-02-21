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

    public Client(String IP) {
        try {
            MotorController motorController = new MotorController();
            socket = new Socket(IP, 5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            while (!(message = in.readUTF()).equals("exit")) {
                System.out.println("Received message: " + message);
                switch (message) {
                    case "FORWARD":
                        motorController.moveForward();
                        break;
                    case "BACKWARD":
                        motorController.moveBackward();
                        break;
                    case "STOP":
                        motorController.stop();
                        break;
                    case "LEFT":
                        // need to implement
                        break;
                    case "RIGHT":
                        // need to implement
                        break;
                }
            }
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
