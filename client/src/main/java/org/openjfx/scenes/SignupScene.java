package org.openjfx.scenes;

import org.openjfx.ChatHandler;
import org.openjfx.ProtocolHandler.RegisterResponse;
import org.openjfx.scenes.components.DisconnectButton;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class SignupScene extends Scene {
    private TextField emailField, userField, nameField;
    private PasswordField passField, confirmField;
    private Label emailLabel, userLabel, nameLabel, passLabel, confirmLabel;

    public SignupScene(Stage stage) {
        super(new AnchorPane(), 600, 400);
        AnchorPane root = (AnchorPane) getRoot();
        root.getStyleClass().add("root");
        root.setStyle("-fx-background-color: #121212;");
        
        if (getClass().getResource("/styles.css") != null) {
            getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }

        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(centerBox, 30.0);
        AnchorPane.setBottomAnchor(centerBox, 30.0);
        AnchorPane.setLeftAnchor(centerBox, 0.0);
        AnchorPane.setRightAnchor(centerBox, 0.0);

        // Fields
        VBox emailBox = createFieldBox("Email", emailField = new TextField(), emailLabel = new Label("Email"));
        VBox userBox = createFieldBox("Username", userField = new TextField(), userLabel = new Label("Username"));
        VBox nameBox = createFieldBox("Display Name", nameField = new TextField(), nameLabel = new Label("Display Name"));
        VBox passBox = createFieldBox("Password", passField = new PasswordField(), passLabel = new Label("Password"));
        VBox confirmBox = createFieldBox("Confirm Password", confirmField = new PasswordField(), confirmLabel = new Label("Confirm Password"));

        // Buttons
        HBox btnBox = new HBox(30);
        btnBox.setAlignment(Pos.CENTER);
        
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("send-btn");
        backBtn.setStyle("-fx-background-color: #222225; -fx-background-radius: 15; -fx-text-fill: #fff8e7; -fx-font-size: 15px;");
        backBtn.setPrefWidth(110);
        backBtn.setOnAction(e -> stage.setScene(new HomeScene(stage)));

        Button signupBtn = new Button("Sign Up");
        signupBtn.getStyleClass().add("send-btn");
        signupBtn.setStyle("-fx-background-color: #222225; -fx-background-radius: 15; -fx-text-fill: #fff8e7; -fx-font-size: 15px;");
        signupBtn.setPrefWidth(110);
        signupBtn.setOnAction(e -> handleSignup(stage));

        btnBox.getChildren().addAll(backBtn, signupBtn);

        centerBox.getChildren().addAll(emailBox, userBox, nameBox, passBox, confirmBox, btnBox);

        DisconnectButton disconnectBtn = new DisconnectButton();
        
        AnchorPane.setBottomAnchor(disconnectBtn, 14.0);
        AnchorPane.setLeftAnchor(disconnectBtn, 14.0);

        root.getChildren().addAll(centerBox, disconnectBtn);
    }

    private VBox createFieldBox(String labelText, TextField field, Label label) {
        VBox box = new VBox(5);
        box.setMaxWidth(240);
        box.setAlignment(Pos.TOP_LEFT);
        label.setStyle("-fx-text-fill: #fff8e7; -fx-font-weight: bold; -fx-font-size: 18px;");
        field.getStyleClass().add("search-field");
        field.setStyle("-fx-background-color: #222225; -fx-text-fill: #fff8e7; -fx-background-radius: 8;");
        field.setAlignment(Pos.CENTER);
        field.setMaxWidth(240);
        box.getChildren().addAll(label, field);
        return box;
    }

    private void handleSignup(Stage stage) {
        String email = emailField.getText();
        String user = userField.getText();
        String name = nameField.getText();
        String pass = passField.getText();
        String confirm = confirmField.getText();
        boolean fail = false;

        if (email.isEmpty()) {
            emailLabel.setText("Please Enter Email");
            fail = true;
        } else if (!email.contains("@") || !email.contains(".")) {
            emailLabel.setText("Invalid Email");
            fail = true;
        } else {
            emailLabel.setText("Email");
        }

        if (user.isEmpty()) {
            userLabel.setText("Please Enter Username");
            fail = true;
        } else {
            userLabel.setText("Username");
        }

        if (pass.isEmpty()) {
            passLabel.setText("Please Enter Password");
            fail = true;
        } else {
            passLabel.setText("Password");
        }

        if (confirm.isEmpty()) {
            confirmLabel.setText("Please Enter Confirm");
            fail = true;
        } else if (!confirm.equals(pass)) {
            confirmLabel.setText("Not matching");
            fail = true;
        } else {
            confirmLabel.setText("Confirm Password");
        }

        if (fail) return;

        try {
            ChatHandler chatHandler = new ChatHandler();
            RegisterResponse response = chatHandler.register(user, pass, name, email);
            System.out.println("Signup Response: " + response.success + ", " + response.message);
            
            if (response.success) {
                System.out.println("Registered successfully. ID: " + response.userId);
                stage.setScene(new LoginScene(stage));
            } else {
                System.err.println("Registration failed: " + response.message);
                emailLabel.setText(response.message != null ? response.message : "Registration Failed");
                emailLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
        } catch (IOException e) {
            e.printStackTrace();
            emailLabel.setText("Connection Error");
        }
    }
}
