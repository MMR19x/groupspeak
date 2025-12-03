package org.openjfx.scenes;

import org.openjfx.scenes.components.DisconnectButton;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeScene extends Scene {

  public HomeScene(Stage stage) {
    super(new AnchorPane(), 600, 400);
    AnchorPane root = (AnchorPane)getRoot();
    root.getStyleClass().add("root");
    root.setStyle("-fx-background-color: #121212;");

    if (getClass().getResource("/styles.css") != null) {
      getStylesheets().add(
          getClass().getResource("/styles.css").toExternalForm());
    }

    VBox centerBox = new VBox(20);
    centerBox.setAlignment(Pos.CENTER);

    Button loginBtn = createButton("Login");
    loginBtn.setOnAction(e -> stage.setScene(new LoginScene(stage)));

    Button signupBtn = createButton("Sign up");
    signupBtn.setOnAction(e -> stage.setScene(new SignupScene(stage)));

    centerBox.getChildren().addAll(loginBtn, signupBtn);

    AnchorPane.setTopAnchor(centerBox, 147.0);
    AnchorPane.setLeftAnchor(centerBox, 0.0);
    AnchorPane.setRightAnchor(centerBox, 0.0);

    DisconnectButton disconnectBtn = new DisconnectButton();

    AnchorPane.setBottomAnchor(disconnectBtn, 25.0);
    AnchorPane.setLeftAnchor(disconnectBtn, 20.0);

    root.getChildren().addAll(centerBox, disconnectBtn);
  }

  private Button createButton(String text) {
    Button btn = new Button(text);
    btn.getStyleClass().add("simple-btn");
    btn.setStyle(
        "-fx-background-color: #222225; -fx-background-radius: 15; " +
        "-fx-text-fill: #fff8e7; -fx-font-size: 20px; -fx-font-weight: bold;");
    btn.setPrefWidth(200);
    return btn;
  }
}
