package org.dfproductions.budgeting;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {

        SceneManager sm = new SceneManager(stage);
        sm.switchScene("fxml/MainPage.fxml");
        Main.stage = stage;
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getStage() {
        return stage;
    }
}