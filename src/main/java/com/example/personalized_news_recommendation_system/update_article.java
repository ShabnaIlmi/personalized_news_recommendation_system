package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.time.LocalDate;
import java.util.Optional;

public class update_article {

    @FXML
    public Button manageMainMenu;
    @FXML
    public Button manageExit;
    @FXML
    private TableView<Document> articleTable;
    @FXML
    private TableColumn<Document, String> articleNameColumn;
    @FXML
    private TableColumn<Document, String> categoryColumn;
    @FXML
    private TableColumn<Document, String> authorColumn;
    @FXML
    private TableColumn<Document, String> dateColumn;

    @FXML
    private TextField articleNameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField authorField;
    @FXML
    private DatePicker publishedDatePicker;

    @FXML
    private Button updateArticleButton;
    @FXML
    private Button deleteArticleButton;

    private MongoDatabase database;
    private MongoCollection<Document> articlesCollection;
    private ObservableList<Document> articles = FXCollections.observableArrayList();

    // Setter for MongoDatabase to initialize the database instance
    public void setDatabase(MongoDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException("MongoDatabase cannot be null.");
        }
        this.database = database;
        this.articlesCollection = database.getCollection("Articles");
        loadArticles();
    }

    private void loadArticles() {
        articles.clear();
        if (articlesCollection != null) {
            for (Document doc : articlesCollection.find()) {
                articles.add(doc);
            }
            articleTable.setItems(articles);
        } else {
            showAlert("Database Error", "Failed to load articles from the database.");
        }
    }

    @FXML
    public void initialize() {
        articleNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getString("article_name")));
        categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getString("category")));
        authorColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getString("author")));
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getString("published_date")));

        articleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                articleNameField.setText(newSelection.getString("article_name"));
                categoryField.setText(newSelection.getString("category"));
                authorField.setText(newSelection.getString("author"));
                publishedDatePicker.setValue(LocalDate.parse(newSelection.getString("published_date")));
            }
        });
    }

    @FXML
    public void updateArticle(ActionEvent actionEvent) {
        Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert("Update Error", "Please select an article to update.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Update");
        alert.setHeaderText("Are you sure you want to update this article?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Document updatedArticle = new Document("article_name", articleNameField.getText())
                        .append("author", authorField.getText())
                        .append("category", categoryField.getText())
                        .append("published_date", publishedDatePicker.getValue().toString())
                        .append("content", selectedArticle.getString("content"));

                articlesCollection.updateOne(
                        new Document("article_name", selectedArticle.getString("article_name")),
                        new Document("$set", updatedArticle));

                showAlert("Success", "Article updated successfully!");
                loadArticles();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to update the article.");
            }
        }
    }

    @FXML
    public void deleteArticle(ActionEvent actionEvent) {
        Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert("Delete Error", "Please select an article to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this article?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                articlesCollection.deleteOne(new Document("article_name", selectedArticle.getString("article_name")));
                showAlert("Success", "Article deleted successfully!");
                loadArticles();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to delete the article.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void manageMainMenu(ActionEvent actionEvent) {
        // Navigate to main menu or close this window
    }

    @FXML
    public void manageExit(ActionEvent actionEvent) {
        // Close the update article window
        Stage stage = (Stage) manageExit.getScene().getWindow();
        stage.close();
    }
}
