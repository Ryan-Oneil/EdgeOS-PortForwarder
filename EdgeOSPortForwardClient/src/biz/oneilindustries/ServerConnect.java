package biz.oneilindustries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerConnect {

    private Settings settings;
    private String serverAddress;
    private int serverPort;
    private String serverPassPhrase;
    private ArrayList<Port> currentServerPorts;
    private static final Logger logger = LogManager.getLogger(biz.oneilindustries.ServerConnect.class);

    public ServerConnect() {
        this.settings = new Settings();
        this.serverAddress = settings.getServerAddress();
        this.serverPort = settings.getServerPort();
        this.serverPassPhrase = settings.getPassPhase();
        this.currentServerPorts = new ArrayList<>();
    }

    public boolean sendPort(Port port) {
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            socket.setSoTimeout(20000);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            String option = "sendPort:" + serverPassPhrase;
            objectOutputStream.writeObject(option);
            objectOutputStream.writeObject(port);
            objectOutputStream.flush();
            if (objectInputStream.readObject().toString().equals("success")) {
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error Sending port", e);
        }
        return false;
    }

    public ArrayList<Port> getPortsFromServer() {
        currentServerPorts.clear();
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            socket.setSoTimeout(5000);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            String option = "getPorts:" + serverPassPhrase;
            objectOutputStream.writeObject(option);

            Port port;
            while ((port = (Port) objectInputStream.readObject()) != null) {
                currentServerPorts.add(port);
            }
            return currentServerPorts;
        } catch (ClassNotFoundException | IOException e) {
            logger.error("Error getting ports", e);
        }
        return null;
    }
}