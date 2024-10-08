package org.dfproductions.budgeting.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;
import org.dfproductions.budgeting.backend.templates.DataSingelton;
import org.dfproductions.budgeting.backend.templates.Record;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class RecordEditController implements Initializable {

    @FXML
    private Label statusReportLabel;

    @FXML
    private ChoiceBox<String> categoryChoiceBox;

    @FXML
    private ChoiceBox<String> typeChoiceBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button btnGraphs;

    @FXML
    private Button btnMainPage;

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnWIP;

    @FXML
    private TextField noteField;

    @FXML
    private TextField placeField;

    @FXML
    private TextField priceField;

    DataSingelton recordInstance = DataSingelton.getInstance();

    private static Record record;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categoryChoiceBox.getItems().add("Groceries");
        categoryChoiceBox.getItems().add("Communication");
        categoryChoiceBox.getItems().add("Transport");
        categoryChoiceBox.getItems().add("Education");
        categoryChoiceBox.getItems().add("Healthcare");
        categoryChoiceBox.getItems().add("Supplies");
        categoryChoiceBox.getItems().add("Home");
        categoryChoiceBox.getItems().add("Pet");

        typeChoiceBox.getItems().add("Expense");
        typeChoiceBox.getItems().add("Income");

        record = recordInstance.getRecord();

        typeChoiceBox.setValue(record.getType());
        categoryChoiceBox.setValue(record.getCategory());
        datePicker.setValue(LocalDate.parse(record.getDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        priceField.setText(Double.toString(record.getPrice()));
        placeField.setText(record.getPlace());
        noteField.setText(record.getNote());
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
        if(event.getSource() == btnMainPage) {
            sm.switchScene("fxml/MainPage.fxml");
        }


    }

    @FXML
    private void onCancelClick() throws IOException {
        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/MainPage.fxml");
    }

    @FXML
    private void onSaveClick() {

        if(typeChoiceBox.getValue() == null) {
            turnOnStatusReportLabel("Select a type for the record.");
            return;
        }

        if(categoryChoiceBox.getValue() == null) {
            turnOnStatusReportLabel("Select a category for the record.");
            return;
        }

        Pattern pricePattern = Pattern.compile("^\\d+(\\.\\d+)?$");
        if(!pricePattern.matcher(priceField.getText()).matches()) {
            turnOnStatusReportLabel("Enter a valid price.");
            return;
        }

        if(placeField.getText().isEmpty()) {
            turnOnStatusReportLabel("Enter a place.");
            return;
        }

        if(datePicker.getValue() == null) {
            turnOnStatusReportLabel("Enter a date.");
            return;
        }

        try {

            SceneManager sm = new SceneManager(Main.getStage());

            HttpClient client = HttpClient.newHttpClient();

            String apiUrl = "http://localhost:8080/api/record/update";
            String bodyParams = "{\n" +
                    "      \"id\":\"" + record.getId() + "\",\n" +
                    "      \"category\":\"" + categoryChoiceBox.getValue() + "\",\n" +
                    "      \"date\":\"" + datePicker.getValue().getYear()+ formatValue(datePicker.getValue().getMonthValue())+ formatValue(datePicker.getValue().getDayOfMonth()) + "\",\n" +
                    "      \"price\":\"" + priceField.getText() + "\",\n" +
                    "      \"place\":\"" + placeField.getText() + "\",\n" +
                    "      \"note\":\"" + noteField.getText() + "\",\n" +
                    "      \"type\":\"" + typeChoiceBox.getValue() + "\"\n" +
                    "}"; // JSON body
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

            if(responseCode == 200)
                sm.switchScene("fxml/MainPage.fxml");
            System.out.println(response.statusCode());
            System.out.println(response.body());

            datePicker.setValue(null);
            categoryChoiceBox.setValue("");
            typeChoiceBox.setValue(null);
            priceField.setText("");
            placeField.setText("");
            noteField.setText("");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String formatValue(int value) {

        if(value <= 9)
            return "0" + value;
        else
            return Integer.toString(value);
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
}
