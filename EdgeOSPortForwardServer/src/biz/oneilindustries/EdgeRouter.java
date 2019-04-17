package biz.oneilindustries;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EdgeRouter {

    private String username;
    private String password;
    private BasicCookieStore httpCookieStore;
    private HttpClient edgeClient;
    private String url;
    private JsonObject edgeRouterInformation;
    private static final Logger logger = LogManager.getLogger(EdgeRouter.class);

    public EdgeRouter(String username, String password, String url) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        this.username = username;
        this.password = password;
        this.url = url;
        this.httpCookieStore = new BasicCookieStore();

        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
        SSLContext sslContext = SSLContextBuilder
            .create()
            .loadTrustMaterial(new TrustSelfSignedStrategy())
            .build();
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
        edgeClient = HttpClients.custom().setSSLHostnameVerifier(allowAllHosts).setSSLSocketFactory(connectionFactory).setDefaultCookieStore(httpCookieStore).build();
    }

    public void setEdgeRouterInformation(JsonObject edgeRouterInformation) {
        this.edgeRouterInformation = edgeRouterInformation.getAsJsonObject("FEATURE");
    }

    public boolean getCookie() {
        try {
            List<NameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("username", this.username));
            formData.add(new BasicNameValuePair("password", this.password));
            UrlEncodedFormEntity encodedFormData = new UrlEncodedFormEntity(formData, Consts.UTF_8);

            HttpPost edgePost = new HttpPost(this.url);
            edgePost.setEntity(encodedFormData);
            edgeClient.execute(edgePost);
            edgePost.releaseConnection();
            //If the router login is successful then 3 cookies are created. If it isn't successful then only one is created
            return httpCookieStore.getCookies().size() > 1;
        } catch (IOException e) {
            logger.error("Error getting cookie", e);
        }
        return false;
    }

    public JsonObject postToRouter(String PostData) {
        HttpPost edgePost = new HttpPost(this.url + "api/edge/feature.json");
        HttpResponse edgeResponse;
        try {
            edgePost.setEntity(new StringEntity(PostData));
            edgePost.addHeader("X-CSRF-TOKEN", httpCookieStore.getCookies().get(1).getValue());
            edgeResponse = edgeClient.execute(edgePost);

            String response = EntityUtils.toString(edgeResponse.getEntity());
            JsonElement jsonTree = new JsonParser().parse(response);

            return jsonTree.getAsJsonObject();
        } catch (IOException | IndexOutOfBoundsException e) {
            logger.error("Error Posting to Router", e);
        } finally {
            edgePost.releaseConnection();
        }
        return null;
    }

    public String getJsonData(String item) {
        return edgeRouterInformation.getAsJsonObject("data").getAsJsonPrimitive(item).toString().replace("\"", "");
    }

    public JsonArray getPortJson() {
        JsonElement portRulesArray = edgeRouterInformation.getAsJsonObject("data").get("rules-config");
        if (portRulesArray.isJsonArray()) {
            return portRulesArray.getAsJsonArray();
        }
        return new JsonArray();
    }

    public JsonArray getLanConfig() {
        return edgeRouterInformation.getAsJsonObject("data").getAsJsonArray("lans-config");
    }
}
