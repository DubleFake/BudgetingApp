package org.dfproductions.budgeting.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.dfproductions.budgeting.Main;
import org.dfproductions.budgeting.SceneManager;
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

class DataItem {
    private String date;
    private Double price;

    public DataItem(String date, Double price) {
        this.date = date;
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}

public class GraphsController implements Initializable {

    private static String selectionPeriod;

    @FXML
    private Button btnMainPage;

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnWIP;

    @FXML
    private BarChart<String, Number> expensesBarChart;

    @FXML
    ChoiceBox<String> periodChoiceBox;

    @FXML
    private PieChart expensesPieChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private static List<RecordWrapper> retrievedRecords;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        periodChoiceBox.getItems().add("This month");
        periodChoiceBox.getItems().add("Last month");
        periodChoiceBox.getItems().add("Last 3 months");
        periodChoiceBox.getItems().add("Last 6 months");
        periodChoiceBox.getItems().add("Last year");
        periodChoiceBox.getItems().add("Last 2 years");
        periodChoiceBox.getItems().add("All time");
        periodChoiceBox.setValue("This month");

        expensesBarChart.setAnimated(false);

        LocalDate now = LocalDate.now();
        selectionPeriod = Integer.toString(now.getYear()) + now.getMonthValue();


        retrievedRecords = retrieveNewRecordData();
        populateBarChart();
        try {
            populatePieChart();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }

    private void populateBarChart() {
        // Clear previous data, but avoid clearing the X-axis directly
        expensesBarChart.getData().clear();

        // Set chart title
        expensesBarChart.setTitle("Money spent");

        // Create a new data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<XYChart.Data<String, Number>> data = new ArrayList<>();
        series.setName("Money spent");

        // Populate the data from your records
        for (RecordWrapper recordWrapper : retrievedRecords) {
            String date = recordWrapper.getRecord().getDate();
            String year = date.substring(0, 4);
            String month = date.substring(0, 6).substring(4);

            String formattedDate = year + "/" + month;

            // Check if the category already exists
            if (data.stream().anyMatch(x -> x.getXValue().equals(formattedDate))) {
                // Update the existing value
                data.stream()
                        .filter(x -> x.getXValue().equals(formattedDate))
                        .forEach(x -> x.setYValue((Double) x.getYValue() + recordWrapper.getRecord().getPrice()));
            } else {
                // Add new data point
                data.add(new XYChart.Data<>(formattedDate, recordWrapper.getRecord().getPrice()));
            }
        }

        // Add all data points to the series
        series.getData().addAll(data);

        // Add the updated series to the chart
        expensesBarChart.getData().add(series);

        // Add value labels to the bars
        addValueLabels(series);

        // Set axis labels and rotation
        xAxis.setTickLabelRotation(45);
        xAxis.setLabel("Month");
        yAxis.setLabel("Money spent");
    }

    private void addValueLabels(XYChart.Series<String, Number> series) {
        // Iterate over each data point in the series
        for (XYChart.Data<String, Number> data : series.getData()) {
            // Create a new label for each data point
            Label valueLabel = new Label(data.getYValue().toString());

            // Add the label to the chart (on top of the BarChart)
            StackPane barNode = (StackPane) data.getNode();  // The node representing the bar
            barNode.getChildren().add(valueLabel);

            // Adjust the label position dynamically based on the bar
            barNode.parentProperty().addListener((obs, oldParent, newParent) -> {
                if (newParent != null) {
                    // Position the label in the correct spot
                    Platform.runLater(() -> {
                        valueLabel.setTranslateY(-valueLabel.getHeight() - 5);  // Position above the bar
                        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");  // Optional styling
                    });
                }
            });
        }
    }

    private String formatValue(int value) {

        if(value <= 9)
            return "0" + value;
        else
            return Integer.toString(value);
    }

    private void populatePieChart() throws JsonProcessingException {

        List<PieChart.Data> slices = new ArrayList<>();

        for(RecordWrapper recordWrapper : retrievedRecords){

            if(slices.stream().anyMatch(x -> x.getName().equals(recordWrapper.getRecord().getCategory()))) {
                slices.stream()
                        .filter(x -> x.getName().equals(recordWrapper.getRecord().getCategory()))
                        .forEach(x -> x.setPieValue(x.getPieValue() + recordWrapper.getRecord().getPrice()));
            }else {
                slices.add(new PieChart.Data(recordWrapper.getRecord().getCategory(),recordWrapper.getRecord().getPrice()));
            }

        }

        // Add the PieChart.Data objects to an ObservableList
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(slices);

        // Create the PieChart and set the data
        expensesPieChart.setData(pieChartData);
        expensesPieChart.setTitle("Expenses by category");

        expensesPieChart.getData().forEach(data ->
                data.nameProperty().bind(
                        javafx.beans.binding.Bindings.concat(
                                data.getName(), ": ", data.pieValueProperty().doubleValue()
                        )
                )
        );


    }

    private List<RecordWrapper> retrieveNewRecordData() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String apiUrl = "http://localhost:8080/api/record/get/" + selectionPeriod;

            System.out.println(apiUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + Main.getToken())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.statusCode());
            System.out.println(response.body());

            ObjectMapper objectMapper = new ObjectMapper();

           return objectMapper.readValue(response.body(), new TypeReference<>() {});

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    @FXML
    private void onMenuButtonClick(ActionEvent event) throws IOException {

        SceneManager sm = new SceneManager(Main.getStage());

        if(event.getSource() == btnMainPage) {
            sm.switchScene("fxml/MainPage.fxml");
        }
        if(event.getSource() == btnSettings) {
            sm.switchScene("fxml/Settings.fxml");
        }
        if(event.getSource() == btnWIP) {
            sm.switchScene("fxml/WIP.fxml");
        }


    }
    @FXML
    private void onLoadButtonClicked(){

        LocalDate now = LocalDate.now();

        switch (periodChoiceBox.getValue()) {
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

        retrievedRecords = retrieveNewRecordData();

        try {
            populatePieChart();
            populateBarChart();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
