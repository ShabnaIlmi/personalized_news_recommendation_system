package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class update_article {
    @FXML
    public TableView articleTable;
    @FXML
    public TableColumn articleNameColumn;
    @FXML
    public TableColumn categoryColumn;
    @FXML
    public TableColumn authorColumn;
    @FXML
    public TableColumn dateColumn;
    @FXML
    public TextField articleNameField;
    @FXML
    public TextField categoryField;
    @FXML
    public TextField authorField;
    @FXML
    public DatePicker publishedDatePicker;
    @FXML
    public Button updateArticleButton;
    @FXML
    public Button deleteArticleButton;
    @FXML
    public Button manageMainMenu;
    @FXML
    public Button manageExit;
    @FXML
    public void manageMainMenu(ActionEvent actionEvent) {
    }
    @FXML
    public void manageExit(ActionEvent actionEvent) {
    }
    @FXML
    public void updateArticle(ActionEvent actionEvent) {
    }
    @FXML
    public void deleteArticle(ActionEvent actionEvent) {
    }

    public void setDatabase(MongoDatabase database) {

    }
}
