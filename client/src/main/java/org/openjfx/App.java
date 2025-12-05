package org.openjfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.openjfx.scenes.ConnectScene;

import java.io.IOException;

/**
 * Main Application Class for the "GroupSpeak" Chat Application.
 * This class handles the entire UI setup, event handling, and mock data management.
 * It uses JavaFX for rendering the GUI.
 */
public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new ConnectScene(stage);
        stage.setTitle("GroupSpeak");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}