package org.detection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Server implements PropertyChangeListener {

    Socket socket;
    ServerSocket serverSocket;
    Thread scanThread, clientThread, tachoThread;
    String command = "";

    ObjectRecog objectRecog;

    Scanner scanner;
    ArrayList<DataOutputStream> outputStreams;
    ArrayList<DataInputStream> inputStreams;
    ArrayList<Thread> threads;
    int mode = 0;

    DetectedObject object = null;

    public Server(ObjectRecog objectRecog) {
        this.objectRecog = objectRecog;
        outputStreams = new ArrayList<>();
        inputStreams = new ArrayList<>();
        threads = new ArrayList<>();
        scanner = new Scanner(System.in);

        objectRecog.addPropertyChangeListener(this);


        scanThread = new Thread(objectRecog::scan);
        threads.add(new Thread(() -> pollTacho(0)));
        threads.add(new Thread(() -> pollTacho(1)));
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
                inputStreams.add(new DataInputStream(socket.getInputStream()));

                System.out.println("Client connected");

                outputStreams.get(mode).writeUTF(String.valueOf(mode));
                //tachoThread = new Thread(() -> pollTacho(mode));
                mode++;

                /*if (mode == 2) {
                    outputStreams.get(0).writeUTF("FORWARDCTRLD:0");
                    //System.out.println(inputStreams.get(0).readUTF());
                }*/

                /*if (mode == 2) {
                    objectRecog.testRails();
                }*/

               if (mode == 2 && inputStreams.get(0).readUTF().equals("READY") && inputStreams.get(1).readUTF().equals("READY")){
                   //tachoThread.start();
                   threads.get(0).start();
                   threads.get(1).start();
                   outputStreams.get(0).writeUTF("START:0");
                   outputStreams.get(1).writeUTF("START:0");
                   objectRecog.start();
               }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (mode == 2) {
            command = evt.getNewValue() + ":" + objectRecog.getSpeed();
            //System.out.println("Sent command: " + command);
            try {
                if (Objects.equals(evt.getPropertyName(), "CommandY")) {
                    outputStreams.get(1).writeUTF(command);
                    /*if (command.contains("GETTACHO")) {
                        objectRecog.setTachoY(Integer.parseInt(inputStreams.get(1).readUTF()));
                    }*/
                } else if (Objects.equals(evt.getPropertyName(), "CommandX")) {
                    outputStreams.get(0).writeUTF(command);
                    /*if (command.contains("GETTACHO")) {
                        objectRecog.setTachoX(Integer.parseInt(inputStreams.get(0).readUTF()));
                    }*/
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void pollTacho(int mode) {
        //System.out.println("mode: " + mode);
        while (mode == 0) {
            try {
                objectRecog.setTachoX(Integer.parseInt(inputStreams.get(mode).readUTF()));
                //objectRecog.setTachoY(Integer.parseInt(inputStreams.get(1).readUTF()));
                //System.out.println("TachoX: " + objectRecog.getTachoX() + " TachoY: " + objectRecog.getTachoY());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (mode == 1) {
            try {
                objectRecog.setTachoY(Integer.parseInt(inputStreams.get(mode).readUTF()));
                //objectRecog.setTachoY(Integer.parseInt(inputStreams.get(1).readUTF()));
                //System.out.println("TachoX: " + objectRecog.getTachoX() + " TachoY: " + objectRecog.getTachoY());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
