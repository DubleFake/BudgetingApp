package org.dfproductions.budgeting.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class GraphsController implements Initializable {

    private static String selectionPeriod;

    @FXML
    private Button btnGraphs;

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

        LocalDate now = LocalDate.now();
        selectionPeriod = now.getYear() + formatValue(now.getMonthValue());

        retrievedRecords = retrieveNewRecordData();
        try {
            populatePieChart();
            populateBarChart();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private void populateBarChart() {

        xAxis.setLabel("Month");
        yAxis.setLabel("Money spent");
        xAxis.setVisible(true);
        yAxis.setVisible(true);

        expensesBarChart.setTitle("Money spent per month");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Money spent");

        /*for (RecordWrapper recordWrapper : retrievedRecords) {

            String date = recordWrapper.getRecord().getDate();
            String year = date.substring(0,4);
            String month = date.substring(0,6).substring(4);

            String formattedDate = year + "/" + month;

            series.getData().add(formattedDate, );

            if (series.stream().anyMatch(x -> x.getName().equals(formattedDate))) {
                series.stream()
                        .filter(x -> x.getName().equals(formattedDate))
                        .forEach(x -> x.getData().set(0, new XYChart.Data<>("Expenses", x.getData().get(0).getYValue().doubleValue() + recordWrapper.getRecord().getPrice())));
            } else {


                newSeries.getData().add(new XYChart.Data<>("Expenses", 0));
                newSeries.setName(formattedDate);
            }

        }*/

        expensesBarChart.getData().clear();
        expensesBarChart.getData().add(series);

       /* // category = month
        // series = money

        // Create data series
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("2020");

        // Add data to the series
        series1.getData().add(new XYChart.Data<>("Category A", 50));
        series1.getData().add(new XYChart.Data<>("Category B", 80));
        series1.getData().add(new XYChart.Data<>("Category C", 90));

        // Create another series
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("2019");

        // Add data to the second series
        series2.getData().add(new XYChart.Data<>("Category A", 60));
        series2.getData().add(new XYChart.Data<>("Category B", 70));
        series2.getData().add(new XYChart.Data<>("Category C", 85));

        // Add the series to the BarChart
        expensesBarChart.getData().addAll(series1, series2);*/
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
        expensesPieChart.setTitle("Expenses this month by category");

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

        try {
            retrieveNewRecordData();
            populatePieChart();
            populateBarChart();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
