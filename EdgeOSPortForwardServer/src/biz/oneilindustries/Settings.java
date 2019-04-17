package biz.oneilindustries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {

    private String edgeRouterURL;
    private String edgeUsername;
    private String edgePassword;
    private String serverPassword;
    private int serverListenPort;
    private boolean enableDiscordNotify;
    private String discordWebhook;
    private static final Logger logger = LogManager.getLogger(Settings.class);

    public Settings() {
        if (new File("settings.properties").isFile()) {
            getSettings();
        } else {
            this.edgeRouterURL = "https://192.168.1.1/";
            this.edgeUsername = "ubnt";
            this.edgePassword = "ubnt";
            this.serverPassword = "NoneSet";
            this.serverListenPort = 5000;
            this.enableDiscordNotify = false;
            this.discordWebhook = "disabled";
            saveSettings();
        }
    }

    public String getURL() {
        return edgeRouterURL;
    }

    public String getEdgePassword() {
        return edgePassword;
    }

    public String getEdgeUsername() {
        return edgeUsername;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public int getServerListenPort() {
        return serverListenPort;
    }

    public boolean isEnableDiscordNotify() {
        return enableDiscordNotify;
    }

    public String getDiscordWebhook() {
        return discordWebhook;
    }

    public void saveSettings() {
        try (FileOutputStream settingsFile = new FileOutputStream("settings.properties")) {
            Properties settings = new Properties();
            settings.setProperty("EdgeRouterURL", edgeRouterURL);
            settings.setProperty("EdgeRouterUsername", edgeUsername);
            settings.setProperty("EdgeRouterPassword", edgePassword);
            settings.setProperty("ServerPassPhrase", serverPassword);
            settings.setProperty("serverListenPort", "" + serverListenPort);
            settings.setProperty("enableDiscordNotify", "" + enableDiscordNotify);
            settings.setProperty("discordWebhook",discordWebhook);
            settings.store(settingsFile, "EdgeOS Router Settings");
        } catch (IOException e) {
            logger.error("Error Saving Settings", e);
        }
    }

    private void getSettings() {
        try (FileInputStream settingsFile = new FileInputStream("settings.properties")) {
            Properties settings = new Properties();
            settings.load(settingsFile);
            edgeRouterURL = settings.getProperty("EdgeRouterURL");
            edgeUsername = settings.getProperty("EdgeRouterUsername");
            edgePassword = settings.getProperty("EdgeRouterPassword");
            serverPassword = settings.getProperty("ServerPassPhrase");
            serverListenPort = Integer.valueOf(settings.getProperty("serverListenPort"));
            enableDiscordNotify = Boolean.valueOf(settings.getProperty("enableDiscordNotify"));
            discordWebhook = settings.getProperty("discordWebhook");
        } catch (IOException e) {
            logger.error("Error Getting Settings", e);
        }
    }
}
