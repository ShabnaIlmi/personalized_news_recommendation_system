package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

public class update_articles {

    @FXML
    private TableView<Document> articleTable;
    @FXML
    private TableColumn<Document, String> articleIDColumn;
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

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> articlesCollection;

    // Set the MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in Update Articles.");
    }

    // Set the MongoDatabase and initialize the articles collection
    public void setDatabase(MongoDatabase database) {
        this.database = database;
        this.articlesCollection = database.getCollection("Articles");
        populateArticleTable();
    }

    // Populate the article table with documents from MongoDB
    private void populateArticleTable() {
        articleTable.getItems().clear();
        for (Document doc : articlesCollection.find()) {
            articleTable.getItems().add(doc);
        }

        // Set up the table columns with appropriate getters
        articleIDColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getObjectId("_id").toHexString()) // Extract ObjectId and convert to String
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
    }

    // Handle the update button action
    @FXML
    public void updateArticle() {
        try {
            Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if (selectedArticle == null) {
                showAlert("Selection Error", "Please select an article to update.");
                return;
            }

            // Get data from the input fields
            String articleName = articleNameField.getText();
            String category = categoryField.getText();
            String author = authorField.getText();
            String publishedDate = (publishedDatePicker.getValue() != null) ?
                    publishedDatePicker.getValue().toString() : "";

            // Validate input fields
            if (articleName.isEmpty() || category.isEmpty() || author.isEmpty() || publishedDate.isEmpty()) {
                showAlert("Validation Error", "Please fill all fields before updating.");
                return;
            }

            // Create the update document
            Document updatedArticle = new Document("article_name", articleName)
                    .append("category", category)
                    .append("author", author)
                    .append("published_date", publishedDate);

            // Update the article in the collection
            ObjectId articleId = selectedArticle.getObjectId("_id"); // Retrieve ID from selected row
            articlesCollection.updateOne(new Document("_id", articleId),
                    new Document("$set", updatedArticle));

            // Show success message
            showAlert("Success", "Article updated successfully.");

            // Refresh the table
            populateArticleTable();

            // Clear input fields
            clearFields();

        } catch (Exception e) {
            showAlert("Error", "Failed to update the article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handle the delete button action
    @FXML
    public void deleteArticle() {
        try {
            Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if (selectedArticle == null) {
                showAlert("Selection Error", "Please select an article to delete.");
                return;
            }

            // Confirm deletion
            boolean confirmDeletion = showConfirmationAlert("Delete Article", "Are you sure you want to delete this article?");
            if (!confirmDeletion) {
                return;
            }

            // Delete the article from the collection
            ObjectId articleId = selectedArticle.getObjectId("_id");
            articlesCollection.deleteOne(new Document("_id", articleId));

            // Show success message
            showAlert("Success", "Article deleted successfully.");

            // Refresh the table
            populateArticleTable();

        } catch (Exception e) {
            showAlert("Error", "Failed to delete the article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to show an alert dialog
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper method to show a confirmation alert
    private boolean showConfirmationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().get() == ButtonType.OK;
    }

    // Helper method to clear input fields
    private void clearFields() {
        articleNameField.clear();
        categoryField.clear();
        authorField.clear();
        publishedDatePicker.setValue(null);
    }

    // Handle the "Main Menu" button action
    @FXML
    public void manageMainMenu() {
        try {
            // Close the current window
            Stage stage = (Stage) articleNameField.getScene().getWindow();
            stage.close();

            // Open the main menu window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_main_menu.fxml"));
            Parent root = loader.load();
            Stage mainMenuStage = new Stage();
            mainMenuStage.setScene(new Scene(root));
            mainMenuStage.setTitle("Main Menu");
            mainMenuStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the Main Menu: " + e.getMessage());
        }
    }

    // Handle the "Exit" button action
    @FXML
    public void manageExit() {
        // Close the current window
        Stage stage = (Stage) articleNameField.getScene().getWindow();
        stage.close();
    }
}
