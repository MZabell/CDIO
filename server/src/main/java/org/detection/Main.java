package org.detection;

public class Main {

    public static void main(String[] args) {
        ObjectRecog objectRecog = new ObjectRecog();
        Server server = new Server(objectRecog);
        System.exit(0);
    }
}
