package org.openjfx.scenes.components;
import java.io.IOException;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.openjfx.ClientState;
import org.openjfx.scenes.ConnectScene;

public class DisconnectButton extends Button {
  public DisconnectButton() {
    super("Disconnect");
    this.getStyleClass().add("simple-btn");
    this.setStyle(
        "-fx-background-color: #222225; -fx-background-radius: 15; "
        +
        "-fx-text-fill: #fff8e7; -fx-font-size: 15px; -fx-font-weight: bold;");
    this.setPrefWidth(200);

    this.setOnAction(event -> {
      try {
        ClientState.getInstance().getConnection().disconnect();
        Stage root = (Stage)getScene().getRoot().getScene().getWindow();
        this.setOnAction(e -> root.setScene(new ConnectScene(root)));
        root.setScene(new ConnectScene(root));
        System.out.println("Disconnected from server.");
      } catch (IOException e) {
        System.err.println("Error while disconnecting: " + e.getMessage());
      }
    });
  }
}