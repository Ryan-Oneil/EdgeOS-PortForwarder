package biz.oneilindustries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {

    private int serverPort;
    private String passPhrase;
    private String serverIPAddress;
    private String clientName;
    private static final Logger logger = LogManager.getLogger(Settings.class);

    public Settings() {
        if (new File("settings.properties").isFile()) {
            getSettings();
        } else {
            serverIPAddress = "192.168.0.1";
            serverPort = 0;
            passPhrase = "NoneSet";
            clientName = "NotSet";
            saveSettings();
        }
    }

    public String getServerAddress() {
        return serverIPAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getPassPhase() {
        return passPhrase;
    }

    public String getClientName() {
        return clientName;
    }

    public void saveServerAddress(String newServerAddress) {
        this.serverIPAddress = newServerAddress;
    }

    public void saveServerPort(int newServerPort) {
        this.serverPort = newServerPort;
    }

    public void savePassPhase(String newServerPassPhrase) {
        this.passPhrase = newServerPassPhrase;
    }

    public void saveClientName(String clientName) {
        this.clientName = clientName;
    }

    public Boolean saveSettings() {
        try (FileOutputStream settingsFile = new FileOutputStream("settings.properties")) {
            Properties settings = new Properties();
            settings.setProperty("ServerAddress", serverIPAddress);
            settings.setProperty("ServerPort", "" + serverPort);
            settings.setProperty("ServerPassPhrase", passPhrase);
            settings.setProperty("ClientUsername", clientName);
            settings.store(settingsFile, "EdgeOS Router Server Settings");
            return true;
        } catch (IOException e) {
            logger.error("Error Saving Settings", e);
            return false;
        }
    }

    private void getSettings() {
        try (FileInputStream settingsFile = new FileInputStream("settings.properties")) {
            Properties settings = new Properties();
            settings.load(settingsFile);
            try {
                serverIPAddress = settings.getProperty("ServerAddress");
                serverPort = Integer.valueOf(settings.getProperty("ServerPort"));
                passPhrase = settings.getProperty("ServerPassPhrase");
                clientName = settings.getProperty("ClientUsername");
            } catch (NumberFormatException e) {
                logger.error("Error converting string to int", e);
                serverPort = 0;
            }
        } catch (IOException e) {
            logger.error("Error Getting Settings", e);
        }
    }
}
