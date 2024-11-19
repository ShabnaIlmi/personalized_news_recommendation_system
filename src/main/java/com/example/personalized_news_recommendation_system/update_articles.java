package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.bson.Document;

import java.time.LocalDate;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class update_articles {

    @FXML
    private TableView<Article> articleTable;
    @FXML
    private TableColumn<Article, String> articleNameColumn;
    @FXML
    private TableColumn<Article, String> categoryColumn;
    @FXML
    private TableColumn<Article, String> authorColumn;
    @FXML
    private TableColumn<Article, String> dateColumn;
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
    @FXML
    private Button manageMainMenu;
    @FXML
    private Button manageExit;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> articleCollection;

    private ObservableList<Article> articleList;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        this.database = database;
        this.articleCollection = database.getCollection("Articles");
        loadArticles();
    }

    @FXML
    public void initialize() {
        // Initialize table columns
        articleNameColumn.setCellValueFactory(new PropertyValueFactory<>("articleName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));

        // Initialize list
        articleList = FXCollections.observableArrayList();
        articleTable.setItems(articleList);

        // Add listener for row selection
        articleTable.setOnMouseClicked(this::onArticleSelected);
    }

    private void loadArticles() {
        articleList.clear();
        List<Document> articles = articleCollection.find().into(new java.util.ArrayList<>());
        for (Document article : articles) {
            Article a = new Article(
                    article.getString("name"),
                    article.getString("category"),
                    article.getString("author"),
                    article.getString("date")
            );
            articleList.add(a);
        }
    }

    private void onArticleSelected(MouseEvent event) {
        Article selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            articleNameField.setText(selectedArticle.getArticleName());
            categoryField.setText(selectedArticle.getCategory());
            authorField.setText(selectedArticle.getAuthor());
            publishedDatePicker.setValue(LocalDate.parse(selectedArticle.getPublishedDate()));
        }
    }

    @FXML
    private void updateArticle() {
        Article selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an article to update.");
            return;
        }

        String newName = articleNameField.getText();
        String newCategory = categoryField.getText();
        String newAuthor = authorField.getText();
        LocalDate newDate = publishedDatePicker.getValue();

        if (newName.isEmpty() || newCategory.isEmpty() || newAuthor.isEmpty() || newDate == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "All fields must be filled.");
            return;
        }

        // Update in MongoDB
        Document updatedArticle = new Document()
                .append("name", newName)
                .append("category", newCategory)
                .append("author", newAuthor)
                .append("date", newDate.toString());

        articleCollection.updateOne(eq("name", selectedArticle.getArticleName()), new Document("$set", updatedArticle));

        // Refresh UI
        loadArticles();
        clearFields();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Article updated successfully.");
    }

    @FXML
    private void deleteArticle() {
        Article selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an article to delete.");
            return;
        }

        // Delete from MongoDB
        articleCollection.deleteOne(eq("name", selectedArticle.getArticleName()));

        // Refresh UI
        loadArticles();
        clearFields();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Article deleted successfully.");
    }

    @FXML
    private void manageMainMenu() {
        // Navigate back to the main menu
        Stage stage = (Stage) manageMainMenu.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void manageExit() {
        // Exit the application
        Stage stage = (Stage) manageExit.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        articleNameField.clear();
        categoryField.clear();
        authorField.clear();
        publishedDatePicker.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
