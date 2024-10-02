package org.dfproductions.budgeting.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;
import org.dfproductions.budgeting.backend.templates.Record;
import org.dfproductions.budgeting.backend.templates.RecordWrapper;


import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {

    @FXML
    private Button btnGraphs;

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnWIP;

    @FXML
    private TableView<Record> expenseTable;

    @FXML
    private TableColumn<Record, Double> priceTableColumn;

    @FXML
    private TableColumn<Record, String> placeTableColumn;

    @FXML
    private TableColumn<Record, String> noteTableColumn;

    @FXML
    private TableColumn<Record, String> categoryTableColumn;

    @FXML
    private TextField textFieldSearch;

    @FXML
    private TextField noteTextField;

    @FXML
    private TextField placeTextField;

    @FXML
    private TextField priceTextField;

    @FXML
    private ChoiceBox<String> categoryChoiceBox;

    @FXML
    public DatePicker datePicker;

    @FXML
    private Label statusReportLabel;

    private ObservableList<Record> recordList;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getRecordsForTable();
        categoryChoiceBox.getItems().add("Groceries");
        categoryChoiceBox.getItems().add("Communication");
        categoryChoiceBox.getItems().add("Transport");
        categoryChoiceBox.getItems().add("Education");
        categoryChoiceBox.getItems().add("Healthcare");
        categoryChoiceBox.getItems().add("Supplies");
        categoryChoiceBox.getItems().add("Home");
        categoryChoiceBox.getItems().add("Cat");
    }

    private void getRecordsForTable() {

        try {
            HttpClient client = HttpClient.newHttpClient();

            LocalDate now = LocalDate.now();

            String apiUrl = "http://localhost:8080/api/record/get/" + now.getYear() + now.getMonthValue();
            String username = "user";
            String password = "user";

            System.out.println(apiUrl);

            String bodyParams = "user"; // JSON body

            // Encode username:password in Base64 for Basic Authentication
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + encodedAuth) // Add Basic Authentication header
                    .method("GET", HttpRequest.BodyPublishers.ofString(bodyParams)) // Note: method allows GET with body
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String json = response.body();

            ObjectMapper objectMapper = new ObjectMapper();


            categoryTableColumn.setCellValueFactory(new PropertyValueFactory<>("Category"));
            priceTableColumn.setCellValueFactory(new PropertyValueFactory<>("Price"));
            placeTableColumn.setCellValueFactory(new PropertyValueFactory<>("Place"));
            noteTableColumn.setCellValueFactory(new PropertyValueFactory<>("Note"));
            recordList = FXCollections.observableArrayList();

            try {
                List<RecordWrapper> records = objectMapper.readValue(json, new TypeReference<>() {
                });

                for (RecordWrapper recordWrapper : records) {
                    recordList.add(recordWrapper.getRecord());
                }

                expenseTable.setItems(recordList);

            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(response.statusCode());
            System.out.println(response.body());

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @FXML
    private void onMenuButtonClick(ActionEvent event) throws IOException {

        turnOffStatusReportLabel();

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

    @FXML
    private void onAddExpenseClick(ActionEvent event) {

        turnOffStatusReportLabel();

        try {

            SceneManager sm = new SceneManager(Main.getStage());

            HttpClient client = HttpClient.newHttpClient();

            LocalDate date =  datePicker.getValue();
            String formattedDate = date.getYear() + Integer.toString(date.getMonth().getValue()) + date.getDayOfMonth();

            String apiUrl = "http://localhost:8080/api/record/create";
            String bodyParams = "{\n" +
                    "   \"record\":{\n" +
                    "      \"category\":\"" + categoryChoiceBox.getValue() + "\",\n" +
                    "      \"date\":\"" + formattedDate + "\",\n" +
                    "      \"price\":\"" + priceTextField.getText() + "\",\n" +
                    "      \"place\":\"" + placeTextField.getText() + "\",\n" +
                    "      \"note\":\"" + noteTextField.getText() + "\"\n" +
                    "   },\n" +
                    "   \"email\":\"user\"\n" +
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

            if(responseCode == 202)
                sm.switchScene("fxml/MainPage.fxml");
            System.out.println(response.statusCode());
            System.out.println(response.body());

            datePicker.setValue(null);
            categoryChoiceBox.setValue("");
            priceTextField.setText("");
            placeTextField.setText("");
            noteTextField.setText("");

            statusReportLabel.setText("Record added successfully!");
            statusReportLabel.setVisible(true);




        } catch (Exception e) {
            e.printStackTrace();
        }

        getRecordsForTable();

    }

    @FXML
    private void onTextFieldSearch(ActionEvent event) {

    }

    @FXML
    private void turnOffStatusReportLabel() {
        if(statusReportLabel.isVisible()) {
            statusReportLabel.setVisible(false);
            statusReportLabel.setText("");
        }
    }

}