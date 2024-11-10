package com.example.personalized_news_recommendation_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

public class rate_articles {
    @FXML
    public TableView rateTable;
    @FXML
    public ChoiceBox rateType;
    @FXML
    public Button rateExit;
    @FXML
    public TableView rateArticles;
    @FXML
    public TableColumn viewedArticles;

    @FXML
    public void rateType(MouseEvent mouseEvent) {
    }
    @FXML
    public void rateExit(ActionEvent actionEvent) {
    }
}
