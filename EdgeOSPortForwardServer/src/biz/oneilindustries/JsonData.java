package biz.oneilindustries;

public class JsonData {

    private String scenario = ".Port_Forwarding";
    private String action = "apply";
    private PortForwards apply;

    public JsonData(PortForwards apply) {
        this.apply = apply;
    }
}

class JsonDataFinal {

    private JsonData data;

    public JsonDataFinal(JsonData data) {
        this.data = data;
    }

}
