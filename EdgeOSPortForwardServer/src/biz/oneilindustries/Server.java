package biz.oneilindustries;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {

    private Socket socket;
    private Settings settingsFile;
    private EdgeRouter edge;
    private PortForwards portForwards;
    private static final Logger logger = LogManager.getLogger(Server.class);

    public Server(Socket socket) {
        this.socket = socket;
        this.settingsFile = new Settings();
    }

    @Override
    public void run() {
        try {
            this.edge = new EdgeRouter(settingsFile.getEdgeUsername(), settingsFile.getEdgePassword(), settingsFile.getURL());
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            logger.error("Error creating EdgeRouter Client", e);
        }
        System.out.println(socket.getInetAddress().getHostAddress() + " has connected");
        if (EdgeSetup()) {
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                String option = (String) objectInputStream.readObject();

                if (option.trim().equals("getPorts:" + settingsFile.getServerPassword())) {
                    sendPorts(objectOutputStream);
                } else if (option.trim().equals("sendPort:" + settingsFile.getServerPassword())) {
                    getPort(objectOutputStream, objectInputStream);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Server Exception", e);
            } finally {
                try {
                    System.out.println("Ending Connection with " + socket.getLocalAddress().getHostAddress());
                    socket.close();
                } catch (IOException e) {
                    logger.error("Socket Exception", e);
                }
            }
        }else {
            System.out.println("Unable to reach Router. Ending connection with " + socket.getLocalAddress().getHostAddress());
        }
    }

    private void getPort(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        Port port = (Port) objectInputStream.readObject();
        System.out.println("Got Port Data: " + port.getLocalAddress() + " " + port.getPortToForward() + " " + port.getDescription());
        String isValidResult = port.isValid(port);
        String result;
        if (isValidResult.equals("valid")) {
            if (postPort(port)) {
                System.out.println("Successfully posted Port");
                result = "success";
            } else {
                System.out.println("Error posting");
                result = "error";
            }
            objectOutputStream.writeObject(result);
            objectOutputStream.flush();
            if (settingsFile.isEnableDiscordNotify()) {
                DiscordWebhook discordWebhook = new DiscordWebhook(settingsFile.getDiscordWebhook());
                discordWebhook.sendNotification(port.toString());
            }
        } else {
            System.out.println("The Port received is invalid error: " + isValidResult);
        }
    }

    private void sendPorts(ObjectOutputStream objectOutputStream) throws IOException {
        ArrayList<Port> ports = portForwards.getPorts();
        //Added null so the client can determine the end of the data stream
        ports.add(null);
        System.out.println("Sending Client List of Ports");
        for (Port port : ports) {
            objectOutputStream.writeObject(port);
            objectOutputStream.reset();
        }
    }

    //Setups the EdgeRouter information by getting login cookie and ports
    private boolean EdgeSetup() {
        if (edge.getCookie()) {
            JsonObject edgeData = edge.postToRouter("{\"data\":{\"scenario\":\".Port_Forwarding\",\"action\":\"load\"}}");
            edge.setEdgeRouterInformation(edgeData);
            portForwards = new PortForwards(edge.getJsonData("auto-firewall"), edge.getJsonData("hairpin-nat"), edge.getJsonData("wan"),
                edge.getLanConfig());
            portForwards.updatePorts(edge.getPortJson());
            return true;
        }
        return false;
    }

    private boolean postPort(Port port) {
        portForwards.addPort(port);
        Gson gson = new Gson();
        try {
            JsonData jsonData = new JsonData(portForwards);
            JsonDataFinal jsonDataFinal = new JsonDataFinal(jsonData);
            String postData = gson.toJson(jsonDataFinal);
            if (postData.contains("rules-config")) {
                JsonObject response = edge.postToRouter(postData);
                return (response.getAsJsonObject("FEATURE").getAsJsonPrimitive("success").getAsInt()) == 1;
            } else {
                System.out.println("Post Data didn't contain ports, Possible parsing error");
            }
        } catch (JsonParseException e) {
            logger.error("Parsing Exception", e);
        }
        return false;
    }
}