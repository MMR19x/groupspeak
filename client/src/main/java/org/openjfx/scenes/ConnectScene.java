package org.openjfx.scenes;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.openjfx.ClientState;
import org.openjfx.Connection;

public class ConnectScene extends Scene {
    private TextField ipField;
    private TextField portField;
    private Label ipLabel;
    private Label portLabel;

    public ConnectScene(Stage stage) {
        super(new VBox(), 600, 400);
        VBox root = (VBox) getRoot();
        root.getStyleClass().add("root");
        root.setStyle("-fx-background-color: #121212;");
        root.setAlignment(Pos.CENTER);
        root.setSpacing(15);
        
        // Load CSS
        if (getClass().getResource("/styles.css") != null) {
            getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }

        // IP Section
        VBox ipBox = new VBox(10);
        ipBox.setMaxWidth(240);
        ipBox.setAlignment(Pos.TOP_LEFT);
        ipLabel = new Label("IP");
        ipLabel.setStyle("-fx-text-fill: #fff8e7; -fx-font-weight: bold; -fx-font-size: 18px;");
        ipField = new TextField();
        ipField.getStyleClass().add("search-field");
        ipField.setStyle("-fx-background-color: #222225; -fx-text-fill: #fff8e7; -fx-background-radius: 8;");
        ipField.setAlignment(Pos.CENTER);
        ipBox.getChildren().addAll(ipLabel, ipField);

        // Port Section
        VBox portBox = new VBox(10);
        portBox.setMaxWidth(240);
        portBox.setAlignment(Pos.TOP_LEFT);
        portLabel = new Label("Port");
        portLabel.setStyle("-fx-text-fill: #fff8e7; -fx-font-weight: bold; -fx-font-size: 18px;");
        portField = new TextField();
        portField.getStyleClass().add("search-field");
        portField.setStyle("-fx-background-color: #222225; -fx-text-fill: #fff8e7; -fx-background-radius: 8;");
        portField.setAlignment(Pos.CENTER);
        portBox.getChildren().addAll(portLabel, portField);

        // Connect Button
        Button connectBtn = new Button("Connect");
        connectBtn.getStyleClass().add("send-btn");
        connectBtn.setStyle("-fx-background-color: #222225; -fx-background-radius: 15; -fx-text-fill: #fff8e7; -fx-font-size: 15px;");
        connectBtn.setPrefWidth(110);
        connectBtn.setOnAction(e -> handleConnect(stage));
        
        VBox btnBox = new VBox(connectBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new javafx.geometry.Insets(20, 0, 0, 0));

        root.getChildren().addAll(ipBox, portBox, btnBox);
    }

    private void handleConnect(Stage stage) {
        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();
        String ipRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        boolean validationFailed = false;
        
        // IP Validation
        if (ip.isEmpty()) {
            ipLabel.setText("Please Enter IP");
            validationFailed = true;
        } else if (!ip.matches(ipRegex)) {
            ipLabel.setText("Enter valid IP");
            validationFailed = true;
        } else {
             ipLabel.setText("IP");
        }

        // Port Validation
        if (portText.isEmpty()) {
            portLabel.setText("Please Enter Port");
            validationFailed = true;
        } else {
             portLabel.setText("Port");
        }

        if (validationFailed) return;

        try {
            int port = Integer.parseInt(portText);
            if (port < 0 || port > 65535) {
                portLabel.setText("Enter valid Port");
                return;
            }
            
            System.out.println("Your IP is " + ip + " your port is " + portText);
            
            // Establish Connection
            Connection connection = new Connection(ip, port);
            ClientState.getInstance().setConnection(connection);
            
            // Switch to Home Scene
            stage.setScene(new HomeScene(stage));
            
        } catch (NumberFormatException e) {
            portLabel.setText("Enter number for Port");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
