package biz.oneilindustries;


import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class DiscordWebhook {

    private String discordWebhookURL;

    public DiscordWebhook(String discordWebhookURL) {
        this.discordWebhookURL = discordWebhookURL;
    }

    public void sendNotification(String message) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(this.discordWebhookURL);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity("{\"content\": \"" + message + "\"}"));

        httpClient.execute(httpPost);
        httpPost.releaseConnection();
    }
}
