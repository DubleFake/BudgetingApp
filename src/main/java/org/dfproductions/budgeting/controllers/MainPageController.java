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
import javafx.util.Callback;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;
import org.dfproductions.budgeting.backend.templates.DataSingelton;
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
import java.util.*;

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
    private TableColumn<Record, String> dateTableColumn;

    @FXML
    private TableColumn<Record, String> typeTableColumn;

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
    private ChoiceBox<String> recordPeriodChoiceBox;

    @FXML
    private ChoiceBox<String> typeChoiceBox;

    @FXML
    public DatePicker datePicker;

    @FXML
    private Label statusReportLabel;

    @FXML
    private TableColumn<Record, String> editTableColumn;

    private static String selectionPeriod;

    private static List<RecordWrapper> recordWrappers;

    DataSingelton recordInstance = DataSingelton.getInstance();



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

        recordPeriodChoiceBox.getItems().add("This month");
        recordPeriodChoiceBox.getItems().add("Last month");
        recordPeriodChoiceBox.getItems().add("Last 3 months");
        recordPeriodChoiceBox.getItems().add("Last 6 months");
        recordPeriodChoiceBox.getItems().add("Last year");
        recordPeriodChoiceBox.getItems().add("Last 2 years");
        recordPeriodChoiceBox.getItems().add("All time");
        recordPeriodChoiceBox.setValue("This month");

        typeChoiceBox.getItems().add("Expense");
        typeChoiceBox.getItems().add("Income");

        LocalDate now = LocalDate.now();
        selectionPeriod = now.getYear() + formatValue(now.getMonthValue());

        recordWrappers = getRecords();
        assert recordWrappers != null;
        insertRecordsIntoTable(recordWrappers);

        expenseTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Load CSS to remove the arrow from MenuButton
                newScene.getStylesheets().add(Main.class.getResource("fxml/css/MainPage.css").toExternalForm());
            }
        });

        addEditButtonToTable();
    }

    private List<RecordWrapper> declassifyRecords(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<RecordWrapper> records = objectMapper.readValue(json, new TypeReference<>() {
            });

            for (RecordWrapper recordWrapper : records) {
                String date = recordWrapper.getRecord().getDate();
                String year = date.substring(0,4);
                String month = date.substring(0,6).substring(4);
                String day = date.substring(date.length() - 2);
                recordWrapper.getRecord().setDate(year + "/" + month + "/" + day);

            }

            return records;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<RecordWrapper> getRecords() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String apiUrl = "http://localhost:8080/api/record/get/" + selectionPeriod;
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

            System.out.println(response.statusCode());
            System.out.println(response.body());

            return declassifyRecords(response.body());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private void addEditButtonToTable() {
        // Set the CellValueFactory for the column to display the name of the record
        editTableColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(""));

        // Define the custom cell factory for the category column
        Callback<TableColumn<Record, String>, TableCell<Record, String>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Record, String> call(final TableColumn<Record, String> param) {
                return new TableCell<>() {

                    private final MenuButton menuButton = new MenuButton("...");
                    {
                        // Create MenuItems for Edit and Delete
                        MenuItem editItem = new MenuItem("Edit");
                        MenuItem deleteItem = new MenuItem("Delete");

                        // Set actions for Edit and Delete
                        editItem.setOnAction(event -> {
                            turnOffStatusReportLabel();
                            Record record = getTableView().getItems().get(getIndex());
                            try {
                                editRecord(record);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        deleteItem.setOnAction(event -> {
                            Record record = getTableView().getItems().get(getIndex());
                            try {
                                deleteRecord(record);
                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        // Add MenuItems to the MenuButton
                        menuButton.getItems().addAll(editItem, deleteItem);
                    }

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item); // Display the string data (e.g., the name)
                            setGraphic(menuButton); // Display the MenuButton with actions
                        }
                    }
                };
            }
        };

        // Set the custom cell factory for the category column
        editTableColumn.setCellFactory(cellFactory);
    }

    private void editRecord(Record record) throws IOException {

        recordInstance.setRecord(record);
        SceneManager sm = new SceneManager(Main.getStage());
        sm.switchScene("fxml/RecordEdit.fxml");
    }

    private void deleteRecord(Record record) throws IOException, InterruptedException {

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation Dialog");
        confirmationAlert.setHeaderText("Confirmation Required");
        confirmationAlert.setContentText("Are you sure you want to delete this record?");

        // Show the alert and wait for the user response
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        // Check the user's response
        if (result.isPresent() && result.get() == ButtonType.OK) {

            HttpClient client = HttpClient.newHttpClient();

            String apiUrl = "http://localhost:8080/api/record/delete/" + record.getId();
            String username = "user";
            String password = "user";

            // Encode username:password in Base64 for Basic Authentication
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + encodedAuth) // Add Basic Authentication header
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.statusCode());
            System.out.println(response.body());

            if(response.statusCode() == 204) {
                recordWrappers = getRecords();
                assert recordWrappers != null;
                insertRecordsIntoTable(recordWrappers);
                addEditButtonToTable();
            }
        } else {
            System.out.println("User cancelled the action.");
        }
    }

    private void insertRecordsIntoTable(List<RecordWrapper> recordWrappers) {

        categoryTableColumn.setCellValueFactory(new PropertyValueFactory<>("Category"));
        priceTableColumn.setCellValueFactory(new PropertyValueFactory<>("Price"));
        placeTableColumn.setCellValueFactory(new PropertyValueFactory<>("Place"));
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("Date"));
        noteTableColumn.setCellValueFactory(new PropertyValueFactory<>("Note"));
        typeTableColumn.setCellValueFactory(new PropertyValueFactory<>("Type"));
        ObservableList<Record> recordList;
        recordList = FXCollections.observableArrayList();
        for(RecordWrapper recordWrapper : recordWrappers) {
            recordList.add(recordWrapper.getRecord());
        }
        expenseTable.setItems(recordList);
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
    private void onAddExpenseClick() {

        turnOffStatusReportLabel();

        try {

            SceneManager sm = new SceneManager(Main.getStage());

            HttpClient client = HttpClient.newHttpClient();

            LocalDate date = datePicker.getValue();

            String formattedDate = date.getYear() + formatValue(date.getMonthValue()) + formatValue(date.getDayOfMonth());

            String apiUrl = "http://localhost:8080/api/record/create";
            String bodyParams = "{\n" +
                    "   \"record\":{\n" +
                    "      \"category\":\"" + categoryChoiceBox.getValue() + "\",\n" +
                    "      \"date\":\"" + formattedDate + "\",\n" +
                    "      \"price\":\"" + priceTextField.getText() + "\",\n" +
                    "      \"place\":\"" + placeTextField.getText() + "\",\n" +
                    "      \"note\":\"" + noteTextField.getText() + "\",\n" +
                    "      \"type\":\"" + typeChoiceBox.getValue() + "\"\n" +
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

            recordWrappers = getRecords();
            assert recordWrappers != null;
            insertRecordsIntoTable(recordWrappers);
            addEditButtonToTable();


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
    private void onTextFieldSearch() {

        List<String> keywords = Arrays.asList("Category:","Price:","Place:","Date:","Note:","Type:");
        List<RecordWrapper> filteredRecordWrappers = new ArrayList<>();
        String query = textFieldSearch.getText().toLowerCase();

        if(!query.isEmpty()) {
            if(keywords.stream().anyMatch(x -> query.toLowerCase().contains(x.toLowerCase())) && query.split(":").length > 1) {
               String[] args = query.split(":");
                if (!args[1].isEmpty()) {
                    for (RecordWrapper recordWrapper : recordWrappers) {
                        switch (args[0].toLowerCase()) {
                            case "category":
                                if (recordWrapper.getRecord().getCategory().toLowerCase().contains(args[1].toLowerCase()))
                                    filteredRecordWrappers.add(recordWrapper);
                                break;
                            case "price":
                                if (Double.toString(recordWrapper.getRecord().getPrice()).toLowerCase().contains(args[1].toLowerCase()))
                                    filteredRecordWrappers.add(recordWrapper);
                                break;
                            case "place":
                                if (recordWrapper.getRecord().getPlace().toLowerCase().contains(args[1].toLowerCase()))
                                    filteredRecordWrappers.add(recordWrapper);
                                break;
                            case "date":
                                if (recordWrapper.getRecord().getDate().toLowerCase().contains(args[1].toLowerCase()))
                                    filteredRecordWrappers.add(recordWrapper);
                                break;
                            case "note":
                                if (recordWrapper.getRecord().getNote().toLowerCase().contains(args[1].toLowerCase()))
                                    filteredRecordWrappers.add(recordWrapper);
                                break;
                            case "type":
                                if (recordWrapper.getRecord().getType().toLowerCase().contains(args[1].toLowerCase()))
                                    filteredRecordWrappers.add(recordWrapper);
                                break;
                            default:
                                break;
                        }
                    }

                }
            } else {
                for (RecordWrapper recordWrapper : recordWrappers) {
                    if (recordWrapper.getRecord().getType().toLowerCase().contains(query)
                            || recordWrapper.getRecord().getCategory().toLowerCase().contains(query)
                            || Double.toString(recordWrapper.getRecord().getPrice()).toLowerCase().contains(query)
                            || recordWrapper.getRecord().getPlace().toLowerCase().contains(query)
                            || recordWrapper.getRecord().getDate().toLowerCase().contains(query)
                            || recordWrapper.getRecord().getNote().toLowerCase().contains(query)) {
                        filteredRecordWrappers.add(recordWrapper);
                    }
                }
            }
        } else {
            filteredRecordWrappers = recordWrappers;
        }

        insertRecordsIntoTable(filteredRecordWrappers);
        addEditButtonToTable();

    }

    @FXML
    private void turnOffStatusReportLabel() {
        if(statusReportLabel.isVisible()) {
            statusReportLabel.setVisible(false);
            statusReportLabel.setText("");
        }
    }

    @FXML
    private void onLoadButtonClicked(){

        LocalDate now = LocalDate.now();

        switch (recordPeriodChoiceBox.getValue()) {
            case "This month":
                selectionPeriod = now.getYear() + formatValue(now.getMonthValue());
                break;
            case "Last month":
                if(now.getMonth().getValue() == 1) {
                    selectionPeriod = now.getYear() - 1 + "12" + "-"  + now.getYear() + formatValue(now.getMonthValue());
                }else{
                    selectionPeriod = now.getYear() + formatValue(now.getMonth().getValue()-1) + "-" + now.getYear() + formatValue(now.getMonthValue());
                }
                break;
            case "Last 3 months":
                if(now.getMonth().getValue() >= 3) {
                    selectionPeriod = now.getYear() + formatValue(now.getMonthValue()-3) + "-" + now.getYear() + formatValue(now.getMonthValue());
                }else{
                    selectionPeriod = now.getYear()-1 + formatValue(now.getMonthValue()-3) + "-" + now.getYear() + formatValue(now.getMonthValue());
                }
                break;
            case "Last 6 months":
                if(now.getMonth().getValue() >= 6) {
                    selectionPeriod = now.getYear() + formatValue(now.getMonthValue()-6) + "-" + now.getYear() + formatValue(now.getMonthValue());
                }else{
                    selectionPeriod = now.getYear()-1 + formatValue(now.getMonthValue()-6) + "-" + now.getYear() + formatValue(now.getMonthValue());
                }
                break;
            case "Last year":
                selectionPeriod = now.getYear() - 1 + formatValue(now.getMonthValue()) + "-" + now.getYear() + formatValue(now.getMonthValue());
                break;
            case "Last 2 years":
                selectionPeriod = now.getYear() - 2 + formatValue(now.getMonthValue()) + "-" + now.getYear() + formatValue(now.getMonthValue());
                break;
            case "All time":
                selectionPeriod = "197001-" + now.getYear() + formatValue(now.getMonthValue());
                break;
            default:
                break;
        }

        recordWrappers = getRecords();
        assert recordWrappers != null;
        insertRecordsIntoTable(recordWrappers);
        addEditButtonToTable();

    }

}