package org.dfproductions.budgeting.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;
import org.dfproductions.budgeting.backend.PasswordHashing;

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

public class RegisterController implements Initializable {


    @FXML
    private Label statusReportLabel;

    @FXML
    private TextField emailField;

    @FXML
    private TextField nameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPasswordField;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void onRegisterClick() {

        try {

            SceneManager sm = new SceneManager(Main.getStage());
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            String nameRegex = "^[A-Z][a-zA-Z]*$";

            Pattern emailPattern = Pattern.compile(emailRegex);
            Pattern namePattern = Pattern.compile(nameRegex);

            Matcher emailMatcher = emailPattern.matcher(emailField.getText());
            Matcher nameMatcher = namePattern.matcher(nameField.getText());

            if (passwordField.getText().length() < 8) {
                turnOnStatusReportLabel("Password must be at least 8 characters long.");
                return;
            }

            if (!passwordField.getText().equals(repeatPasswordField.getText())) {
                turnOnStatusReportLabel("Passwords must match.");
                return;
            }

            if (!emailMatcher.matches()) {
                turnOnStatusReportLabel("Enter a valid email.");
                return;
            }

            if (!nameMatcher.matches()) {
                turnOnStatusReportLabel("Enter a valid name.");
                return;
            }

            String[] passwordCombo = PasswordHashing.hashPassword(passwordField.getText()).split(":");

            HttpClient client = HttpClient.newHttpClient();

            String apiUrl = "http://localhost:8080/api/user/create";
            String bodyParams = "{\"name\":\"" + nameField.getText() + "\",\"email\":\"" + emailField.getText() + "\",\"passwordHash\":\"" + passwordCombo[1] + "\",\"passwordSalt\":\"" + passwordCombo[0] + "\"}"; // JSON body
            String username = "user";
            String password = "user";

            // Encode username:password in Base64 for Basic Authentication
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + encodedAuth) // Add Basic Authentication header
                    .method("POST", HttpRequest.BodyPublishers.ofString(bodyParams)) // Note: method allows GET with body
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int responseCode = response.statusCode();

            if (responseCode == 201)
                sm.switchScene("fxml/Login.fxml");
            System.out.println(response.statusCode());
            System.out.println(response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void turnOffStatusReportLabel() {
        if (statusReportLabel.isVisible()) {
            statusReportLabel.setVisible(false);
            statusReportLabel.setText("");
        }
    }

    private void turnOnStatusReportLabel(String text) {
        if (!statusReportLabel.isVisible())
            statusReportLabel.setVisible(true);
        statusReportLabel.setText(text);
    }

    @FXML
    void onLogInClick() throws IOException {
        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/Login.fxml");
    }
}
