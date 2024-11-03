package org.dfproductions.budgeting.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecoveryController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private Label infoLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    void onRecoverClick() {

        infoLabel.setVisible(true);
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (!Pattern.compile(emailRegex).matcher(emailField.getText()).matches()) {
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String apiUrl = "http://localhost:8080/api/user/recovery/" + emailField.getText();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + Main.getToken()) // Add Basic Authentication header
                    .method("POST", HttpRequest.BodyPublishers.ofString("")) // Note: method allows GET with body
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(apiUrl);
            System.out.println(response.statusCode());
            System.out.println(response.body());


        }catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    void onLogInClick() throws IOException {
        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/Login.fxml");
    }
}
