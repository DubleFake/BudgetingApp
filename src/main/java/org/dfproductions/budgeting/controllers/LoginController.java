package org.dfproductions.budgeting.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    Button btnLogIn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void onLoginClick() throws IOException {
        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/MainPage.fxml");
    }

    @FXML
    private void onRegisterClick() throws IOException {
        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/Register.fxml");
    }
}
