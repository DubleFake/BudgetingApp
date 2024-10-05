package org.dfproductions.budgeting;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.stage.Stage;
import org.dfproductions.budgeting.backend.PasswordHashing;

import java.io.IOException;

public class Main extends Application {

    private static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {

        SceneManager sm = new SceneManager(stage);
        Main.stage = stage;
        stage.setResizable(false);
        sm.switchScene("fxml/MainPage.fxml");
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getStage() {
        return stage;
    }
}