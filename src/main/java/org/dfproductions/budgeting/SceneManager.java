package org.dfproductions.budgeting;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private final Stage stage;
    private final Map<String, FXMLLoader> loaderCache = new HashMap<>();

    public SceneManager(Stage stage) {
        this.stage = stage;
    }
    // Load a scene by its FXML file path and set it as the active scene
    public void switchScene(String fxmlPath) throws IOException {
        FXMLLoader loader = loaderCache.get(fxmlPath);

        if (loader == null) {
            loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            Parent root = loader.load();
            loaderCache.put(fxmlPath, loader);

            Scene scene = new Scene(root);
            stage.setScene(scene);
        } else {
            Parent root = loader.getRoot();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }

        stage.show();
    }

    // Optional: Get a controller for a specific view
    public Object getController(String fxmlPath) {
        FXMLLoader loader = loaderCache.get(fxmlPath);
        return loader != null ? loader.getController() : null;
    }

}
