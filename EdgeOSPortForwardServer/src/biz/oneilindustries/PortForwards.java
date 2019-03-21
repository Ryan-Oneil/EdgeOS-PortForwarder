package biz.oneilindustries;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PortForwards {

    private String wan;
    @SerializedName("hairpin-nat")
    private String hairpinNAT;
    @SerializedName("auto-firewall")
    private String autoFirewall;
    @SerializedName("lans-config")
    private JsonArray lans;
    @SerializedName("rules-config")
    private ArrayList<Port> activeOpenedPorts;

    public PortForwards(String autoFirewall, String hairpinNAT, String wan, JsonArray lan) {
        this.autoFirewall = autoFirewall;
        this.hairpinNAT = hairpinNAT;
        this.wan = wan;
        this.lans = lan;
        activeOpenedPorts = new ArrayList<>();
    }

    public void addPort(Port port) {
        this.activeOpenedPorts.add(port);
    }

    public void updatePorts(JsonArray ports) {
        Port port;
        Gson gson = new Gson();
        activeOpenedPorts.clear();
        for (JsonElement object : ports) {
            port = gson.fromJson(object, Port.class);
            activeOpenedPorts.add(port);
        }
    }

    public ArrayList<Port> getPorts() {
        return activeOpenedPorts;
    }
}
