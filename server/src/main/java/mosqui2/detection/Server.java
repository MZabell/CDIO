package mosqui2.detection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

// Class for server implementation
// Handles connections between robot and server
public class Server implements PropertyChangeListener {

    Socket socket;
    ServerSocket serverSocket;
    String command = "";

    ObjectRecog objectRecog;

    Scanner scanner;
    ArrayList<DataOutputStream> outputStreams;
    ArrayList<DataInputStream> inputStreams;
    ArrayList<Thread> threads;
    int mode = 0;

    public Server(ObjectRecog objectRecog) {
        this.objectRecog = objectRecog;
        outputStreams = new ArrayList<>();
        inputStreams = new ArrayList<>();
        threads = new ArrayList<>();
        scanner = new Scanner(System.in);

        objectRecog.addPropertyChangeListener(this);


        threads.add(new Thread(() -> pollTacho(0)));
        threads.add(new Thread(() -> pollTacho(1)));

        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server is waiting for client request");

            while ((socket = serverSocket.accept()) != null) {
                outputStreams.add(new DataOutputStream(socket.getOutputStream()));
                inputStreams.add(new DataInputStream(socket.getInputStream()));

                System.out.println("Client connected");

                outputStreams.get(mode).writeUTF(String.valueOf(mode));
                mode++;

                if (mode == 2 && inputStreams.get(0).readUTF().equals("READY") && inputStreams.get(1).readUTF().equals("READY")) {
                    threads.get(0).start();
                    threads.get(1).start();
                    outputStreams.get(0).writeUTF("START:0");
                    outputStreams.get(1).writeUTF("START:0");
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Events fired from ObjectRecog are caught here
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (mode == 2) {
            String property = evt.getPropertyName();
            try {
                switch (property) {
                    case "CommandY":
                        command = evt.getNewValue() + ":" + objectRecog.getSpeedY() + ":" + objectRecog.getTachoPoint().x + ":" + objectRecog.getTachoPoint().y;
                        outputStreams.get(1).writeUTF(command);
                        break;
                    case "CommandX":
                        command = evt.getNewValue() + ":" + objectRecog.getSpeedX() + ":" + objectRecog.getTachoPoint().x + ":" + objectRecog.getTachoPoint().y;
                        outputStreams.get(0).writeUTF(command);
                        break;
                    case "SpeedY":
                        command = objectRecog.getCommandY() + ":" + evt.getNewValue() + ":" + objectRecog.getTachoPoint().x + ":" + objectRecog.getTachoPoint().y;
                        outputStreams.get(1).writeUTF(command);
                        break;
                    case "SpeedX":
                        command = objectRecog.getCommandX() + ":" + evt.getNewValue() + ":" + objectRecog.getTachoPoint().x + ":" + objectRecog.getTachoPoint().y;
                        outputStreams.get(0).writeUTF(command);
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Method for polling the tacho values from motor
    private void pollTacho(int mode) {
        while (mode == 0) {
            try {
                objectRecog.setTachoX(Integer.parseInt(inputStreams.get(mode).readUTF()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (mode == 1) {
            try {
                objectRecog.setTachoY(Integer.parseInt(inputStreams.get(mode).readUTF()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
