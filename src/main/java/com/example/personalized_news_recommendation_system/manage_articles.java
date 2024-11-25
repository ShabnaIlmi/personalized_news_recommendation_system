package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class manage_articles {
    @FXML
    private Button updateButton, deleteButton, manageMainMenu, manageExit;
    @FXML
    private TableView<Document> articleTable;
    @FXML
    private TableColumn<Document, String> articleIDColumn, articleNameColumn, categoryColumn, authorColumn, dateColumn;
    @FXML
    private TextField articleNameField, categoryField, authorField;
    @FXML
    private TextArea manageDescription, manageContent;
    @FXML
    private DatePicker publishedDatePicker;

    private MongoClient mongoClient;
    private MongoCollection<Document> articlesCollection;
    private MongoCollection<Document> updatedArticlesCollection;
    private MongoCollection<Document> deletedArticlesCollection;

    // Set the MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        if (mongoClient != null) {
            System.out.println("MongoClient successfully set in Manage Articles.");
        } else {
            System.err.println("MongoClient is null in Manage Articles.");
        }
    }

    // Set the MongoDatabase and initialize collections
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            this.updatedArticlesCollection = database.getCollection("Updated_Articles");
            this.deletedArticlesCollection = database.getCollection("Deleted_Articles");
            System.out.println("Collections initialized successfully.");
            populateArticleTable(); // Populate table after collections are initialized
        } else {
            System.err.println("Database is null. Cannot initialize collections.");
        }
    }

    // Populate the article table
    private void populateArticleTable() {
        try {
            if (articlesCollection == null) {
                throw new IllegalStateException("Articles collection not initialized.");
            }

            List<Document> articles = articlesCollection.find().into(new ArrayList<>());
            System.out.println("Articles Retrieved: " + articles.size());

            if (articles.isEmpty()) {
                articleTable.setPlaceholder(new Label("No articles available."));
            }

            articleIDColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("article_id"))
            );
            articleNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("article_name"))
            );
            categoryColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("category"))
            );
            authorColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("author"))
            );
            dateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("published_date"))
            );

            articleTable.getItems().setAll(articles);

        } catch (Exception e) {
            showAlert("Error", "Failed to populate article table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update selected article
    @FXML
    public void updateArticle(ActionEvent actionEvent) {
        try {
            Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if (selectedArticle == null) {
                showAlert("Error", "Please select an article to update.");
                return;
            }

            if (!validateInputs()) {
                showAlert("Error", "Please fill in all required fields.");
                return;
            }

            // Get the current time for the update
            String currentTime = java.time.LocalDateTime.now().toString();

            // Prepare the updated article document
            Document updatedArticle = new Document("article_name", articleNameField.getText())
                    .append("category", categoryField.getText())
                    .append("author", authorField.getText())
                    .append("description", manageDescription.getText())
                    .append("content", manageContent.getText())
                    .append("published_date", publishedDatePicker.getValue().toString())
                    .append("article_added_time", currentTime); // Add or update the time

            // Perform the update in the database
            articlesCollection.updateOne(
                    new Document("_id", selectedArticle.getObjectId("_id")),
                    new Document("$set", updatedArticle)
            );

            // Insert the updated article into the 'Updated_Articles' collection
            updatedArticlesCollection.insertOne(updatedArticle);

            // Refresh the table and clear input fields
            populateArticleTable();
            clearFields();

            showAlert("Success", "Article updated successfully.");

        } catch (Exception e) {
            showAlert("Error", "Failed to update article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Delete selected article
    @FXML
    public void deleteArticle(ActionEvent actionEvent) {
        try {
            Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if (selectedArticle == null) {
                showAlert("Error", "Please select an article to delete.");
                return;
            }

            articlesCollection.deleteOne(new Document("_id", selectedArticle.getObjectId("_id")));
            deletedArticlesCollection.insertOne(selectedArticle);

            populateArticleTable();

        } catch (Exception e) {
            showAlert("Error", "Failed to delete article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Show alert dialog
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Validate user inputs
    private boolean validateInputs() {
        return !articleNameField.getText().isEmpty() && !categoryField.getText().isEmpty() && !authorField.getText().isEmpty();
    }

    // Clear input fields
    private void clearFields() {
        articleNameField.clear();
        categoryField.clear();
        authorField.clear();
        manageDescription.clear();
        manageContent.clear();
        publishedDatePicker.setValue(null);
    }

    // Navigate to the main menu
    @FXML
    public void manageMainMenu(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) articleNameField.getScene().getWindow();
            stage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_main_menu.fxml"));
            Stage mainMenuStage = new Stage();
            mainMenuStage.setScene(new Scene(loader.load()));
            mainMenuStage.setTitle("Main Menu");
            mainMenuStage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open the Main Menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Exit the application
    @FXML
    public void manageExit(ActionEvent actionEvent) {
        Stage stage = (Stage) articleNameField.getScene().getWindow();
        stage.close();
    }

    // Initialize method
    @FXML
    public void initialize() {
        System.out.println("Manage Articles Page Initialized.");
    }
}
