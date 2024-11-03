package org.dfproductions.budgeting.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    Button btnLogIn;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void onLoginClick() throws IOException {

        try {

            SceneManager sm = new SceneManager(Main.getStage());

            HttpClient client = HttpClient.newHttpClient();

            String apiUrl = "http://localhost:8080/api/user/login";
            String bodyParams = "{\"email\":\"" + loginField.getText() + "\",\"password\":\""+ passwordField.getText() + "\"}"; // JSON body

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(bodyParams)) // Note: method allows GET with body
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int responseCode = response.statusCode();

            System.out.println(response.statusCode());
            System.out.println(response.body());

            Main.setToken(response.body());

            if(responseCode == 202) {}
                //sm.switchScene("fxml/MainPage.fxml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterClick() throws IOException {
        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/Register.fxml");
    }

    @FXML
    private void forgotPasswordClick() throws IOException {
        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/Recovery.fxml");
    }

}
