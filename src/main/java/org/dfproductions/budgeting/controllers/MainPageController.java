package org.dfproductions.budgeting.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {

    @FXML
    private Button btnGraphs;

    @FXML
    private Button btnMainPage;

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnWIP;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void onMenuButtonClick(ActionEvent event) throws IOException {

        SceneManager sm = new SceneManager(Main.getStage());

        if(event.getSource() == btnGraphs) {
            sm.switchScene("fxml/Graphs.fxml");
        }
        if(event.getSource() == btnSettings) {
            sm.switchScene("fxml/Settings.fxml");
        }
        if(event.getSource() == btnWIP) {
            sm.switchScene("fxml/WIP.fxml");
        }


    }

}