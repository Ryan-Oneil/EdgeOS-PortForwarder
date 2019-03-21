package biz.oneilindustries;

import com.google.common.net.InetAddresses;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class Controller {

    private static Settings settings = new Settings();
    private static ServerConnect serverConnect;
    //Port Forward Home page options
    @FXML
    private ChoiceBox protocolChoiceBox;
    @FXML
    private TextField localIPAddress;
    @FXML
    private TextField portToForward;
    @FXML
    private TextField originalPort;
    @FXML
    private TextField portDescription;
    @FXML
    private Button portSubmitButton;
    //End of home page options
    @FXML
    private AnchorPane settingsPage;
    @FXML
    private AnchorPane homePage;
    @FXML
    private AnchorPane portsPage;
    @FXML
    private TextField serverAddress;
    @FXML
    private TextField serverPort;
    @FXML
    private TextField serverPassphrase;
    @FXML
    private TextField clientUsername;
    @FXML
    private TableColumn portCol;
    @FXML
    private TableColumn protocolCol;
    @FXML
    private TableColumn addressCol;
    @FXML
    private TableColumn descriptionCol;
    @FXML
    private TableView portsTable;
    @FXML
    private Button getPortsButton;
    private Alert alert;

    public void initialize() {

        //Setting up GUI elements
        portCol.setCellValueFactory(new PropertyValueFactory<>("portToForward"));
        protocolCol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("localAddress"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        protocolChoiceBox.getSelectionModel().select(0);
        serverAddress.setText("" + settings.getServerAddress());
        serverPort.setText("" + settings.getServerPort());
        serverPassphrase.setText(settings.getPassPhase());
        clientUsername.setText(settings.getClientName());

        alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("dialogue.css").toExternalForm());

        try {
            InetAddress ip = Inet4Address.getLocalHost();
            localIPAddress.setText(ip.getHostAddress().trim());
        } catch (UnknownHostException e) {
            localIPAddress.setText("Unable to get LocalIP");
        }
    }

    @FXML
    public void submitButton() {
        serverConnect = new ServerConnect();
        portSubmitButton.setDisable(true);
        if (portToForward.getText().isEmpty() || localIPAddress.getText().isEmpty() || originalPort.getText().isEmpty()) {
            portSubmitButton.setDisable(false);
        } else {
            Runnable task = () -> {
                Port port = new Port(originalPort.getText().trim(),
                    protocolChoiceBox.getValue().toString().trim(),
                    localIPAddress.getText().trim(),
                    portToForward.getText().trim(),
                    portDescription.getText().trim() + " added by " + clientUsername.getText().trim());
                String isValidResult = port.isValid(port);
                Boolean success = false;
                if (isValidResult.equals("valid")) {
                    success = serverConnect.sendPort(port);
                    if (!success) {
                        isValidResult = "Unable to send Port to Server";
                    }
                }
                String finalIsValidResult = isValidResult;
                Boolean finalSuccess = success;
                Platform.runLater(() -> {
                    portSubmitButton.setDisable(false);
                    alert.setHeaderText("Port Status");
                    if (finalSuccess) {
                        alert.setAlertType(Alert.AlertType.INFORMATION);
                        alert.setContentText("Port successfully forwarded");
                    } else {
                        alert.setContentText(finalIsValidResult);
                    }
                    alert.show();
                });
            };
            new Thread(task).start();
        }
    }

    @FXML
    public void openHomePage() {
        if (!homePage.isVisible()) {
            settingsPage.setVisible(false);
            portsPage.setVisible(false);
            homePage.setVisible(true);
        }
    }

    @FXML
    public void openSettingsPage() {
        if (!settingsPage.isVisible()) {
            homePage.setVisible(false);
            portsPage.setVisible(false);
            settingsPage.setVisible(true);
        }
    }

    @FXML
    public void openPortsPage() {
        if (!portsPage.isVisible()) {
            homePage.setVisible(false);
            settingsPage.setVisible(false);
            portsPage.setVisible(true);
        }
    }

    @FXML
    public void saveButton() {
        if (InetAddresses.isInetAddress(serverAddress.getText().trim())) {
            settings.saveServerAddress(serverAddress.getText());
            settings.saveServerPort(Integer.valueOf(serverPort.getText()));
            settings.savePassPhase(serverPassphrase.getText());
            settings.saveClientName(clientUsername.getText());
            if (!settings.saveSettings()) {
                alert.setContentText("Unable to save settings");
                alert.show();
            }
        } else {
            alert.setContentText("Invalid IP Address");
            alert.show();
        }
    }

    @FXML
    public void addPorts() {
        getPortsButton.setDisable(true);
        Runnable task = () -> {
            serverConnect = new ServerConnect();
            ArrayList<Port> ports = serverConnect.getPortsFromServer();
            Platform.runLater(() -> {
                portsTable.getItems().clear();
                if (ports != null) {
                    for (Port port : ports) {
                        portsTable.getItems().add(port);
                    }
                }
                getPortsButton.setDisable(false);
            });
        };
        new Thread(task).start();
    }
}
