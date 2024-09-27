package org.dfproductions.budgeting.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void onRegisterClick() throws IOException {

        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/Login.fxml");

    }
}
