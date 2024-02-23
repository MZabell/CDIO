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
import java.util.Scanner;

public class Server implements PropertyChangeListener {

    Socket socket;
    ServerSocket serverSocket;
    Thread scanThread, clientThread;
    String command = "";

    ObjectRecog objectRecog;

    Scanner scanner;
    ArrayList<DataOutputStream> outputStreams;
    ArrayList<Thread> threads;
    int mode = 0;

    public Server(ObjectRecog objectRecog) {
        this.objectRecog = objectRecog;
        outputStreams = new ArrayList<>();
        threads = new ArrayList<>();
        scanner = new Scanner(System.in);

        objectRecog.addPropertyChangeListener(this);


        scanThread = new Thread(objectRecog::scan);
        scanThread.start();
        new Thread(() -> {
            if (scanner.hasNext()) {
                for (DataOutputStream out : outputStreams) {
                    try {
                        out.writeUTF("STOP");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.exit(0);
            }
        }).start();

        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server is waiting for client request");

            while ((socket = serverSocket.accept()) != null) {
                outputStreams.add(new DataOutputStream(socket.getOutputStream()));

                System.out.println("Client connected");

                outputStreams.get(mode).writeUTF(String.valueOf(mode));
                mode++;

               if (mode == 2) {
                  runClient();
                }

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

    private void runClient() {

        DetectedObject object;

        while (!objectRecog.getQueue().isEmpty()) {
            object = objectRecog.getQueue().peek();
            objectRecog.sendCommand(object);
        }
    }
}
