package org.detection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements PropertyChangeListener {

    Socket socket;
    ServerSocket serverSocket;
    DataInputStream in;
    DataOutputStream out;
    Thread scanThread;
    String command = "";

    public Server(ObjectRecog objectRecog) {

        objectRecog.addPropertyChangeListener(this);

        scanThread = new Thread(objectRecog::scan);
        scanThread.start();

        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server is waiting for client request");

            while ((socket = serverSocket.accept()) != null) {

                System.out.println("Client connected");

                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                DetectedObject object;

                while (!objectRecog.getQueue().isEmpty()) {
                    object = objectRecog.getQueue().peek();
                    objectRecog.sendCommand(object);
                }

                System.out.println("Out of loop");

                in.close();
                out.close();
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        command = (String) evt.getNewValue();
        System.out.println("Sent command: " + command);
        try {
            out.writeUTF(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
