package biz.oneilindustries;

import com.google.common.net.InetAddresses;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Port implements Serializable {

    @SerializedName("original-port")
    private String originalPort;
    private String protocol;
    @SerializedName("forward-to-address")
    private String localAddress;
    @SerializedName("forward-to-port")
    private String portToForward;
    private String description;

    public Port(String originalPort, String protocol, String localAddress, String portToForward, String description) {
        this.originalPort = originalPort;
        this.protocol = protocol;
        this.localAddress = localAddress;
        this.portToForward = portToForward;
        this.description = description;
    }

    public String getOriginalPort() {
        return originalPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public String getPortToForward() {
        return portToForward;
    }

    public String getDescription() {
        return description;
    }

    public String isValid(Port port) {
        if (port != null) {
            if (!port.getOriginalPort().matches("^[0-9]+$")) {
                return "Invalid OriginalPort";
            }
            if(!port.getProtocol().contains("tcp") && !port.getProtocol().contains("udp")) {
                return "Invalid Protocol";
            }
            if (!InetAddresses.isInetAddress(port.getLocalAddress())) {
                return "Invalid IPAddress";
            }
            if(!port.getPortToForward().matches("^[0-9]+$")) {
                return "Invalid PortToForward";
            }
            if (!port.getDescription().matches("^[a-zA-Z0-9 ]+$")) {
                return "Invalid Description, Please use a-z and 0-9, no symbols";
            }
            return "valid";
        }
        return "Null Port";
    }

    @Override
    public String toString() {
        return "Port{" +
            "originalPort='" + originalPort + '\'' +
            ", protocol='" + protocol + '\'' +
            ", localAddress='" + localAddress + '\'' +
            ", portToForward='" + portToForward + '\'' +
            ", description='" + description + '\'' +
            '}';
    }
}