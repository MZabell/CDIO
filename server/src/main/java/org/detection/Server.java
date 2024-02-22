package org.detection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements PropertyChangeListener {

    Socket socket;
    ServerSocket serverSocket;
    Thread scanThread, clientThread;
    String command = "";

    ObjectRecog objectRecog;

    ArrayList<DataOutputStream> outputStreams;
    int mode = 0;

    public Server(ObjectRecog objectRecog) {
        this.objectRecog = objectRecog;
        outputStreams = new ArrayList<>();

        objectRecog.addPropertyChangeListener(this);


        scanThread = new Thread(objectRecog::scan);
        scanThread.start();

        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server is waiting for client request");

            while ((socket = serverSocket.accept()) != null) {
                outputStreams.add(new DataOutputStream(socket.getOutputStream()));

                System.out.println("Client connected");

                clientThread = new Thread(() -> {
                    try {
                        outputStreams.get(mode).writeUTF(String.valueOf(mode));
                        mode++;
                        runClient();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                clientThread.start();

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
            for (DataOutputStream out : outputStreams) {
                out.writeUTF(command);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runClient() throws IOException {

        DetectedObject object;

        while (!objectRecog.getQueue().isEmpty()) {
            object = objectRecog.getQueue().peek();
            objectRecog.sendCommand(object);
        }
    }
}
