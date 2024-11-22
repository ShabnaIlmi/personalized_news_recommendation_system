package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class manage_articles {
    @FXML
    public Button manageMainMenu;
    @FXML
    public Button manageExit;
    @FXML
    private TableView<Document> articleTable;
    @FXML
    private TextField articleNameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField authorField;
    @FXML
    private TextArea manageDescription;
    @FXML
    private TextArea manageContent;
    @FXML
    private TextField publishedDateField;

    private MongoClient mongoClient;
    private MongoCollection<Document> articlesCollection;
    private MongoCollection<Document> updatedArticlesCollection;
    private MongoCollection<Document> deletedArticlesCollection;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for MongoDatabase and collections
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            this.updatedArticlesCollection = database.getCollection("Updated_Articles");
            this.deletedArticlesCollection = database.getCollection("Deleted_Articles");

            // If the collection doesn't exist yet, it will be created when a document is inserted
            if (this.deletedArticlesCollection == null) {
                this.deletedArticlesCollection = database.getCollection("Deleted_Articles");
            }
        }
    }

    // Method to update an article
    @FXML
    public void updateArticle() {
        executorService.submit(() -> {
            try {
                Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
                if (selectedArticle == null) {
                    showAlert("Error", "Please select an article to update.");
                    return;
                }

                String articleName = articleNameField.getText();
                String category = categoryField.getText();
                String author = authorField.getText();
                String publishedDate = publishedDateField.getText();
                String description = manageDescription.getText();
                String content = manageContent.getText();

                if (articleName.isEmpty() || category.isEmpty() || author.isEmpty() || publishedDate.isEmpty()) {
                    showAlert("Error", "Please fill all fields before updating.");
                    return;
                }

                // Prepare the updated article document
                Document updatedArticle = new Document("article_name", articleName)
                        .append("category", category)
                        .append("author", author)
                        .append("published_date", publishedDate)
                        .append("description", description)
                        .append("content", content);

                ObjectId articleId = selectedArticle.getObjectId("_id");
                articlesCollection.updateOne(new Document("_id", articleId),
                        new Document("$set", updatedArticle));

                // Log the update in Updated_Articles collection
                String updatedAt = Instant.now()
                        .atZone(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ISO_INSTANT);
                Document updateLog = new Document("article_id", selectedArticle.getString("article_id"))
                        .append("article_name", articleName)
                        .append("author", author)
                        .append("published_date", publishedDate)
                        .append("description", description)
                        .append("content", content)
                        .append("category", category)
                        .append("updated_at", updatedAt);
                updatedArticlesCollection.insertOne(updateLog);

                Platform.runLater(() -> {
                    showAlert("Success", "Article updated successfully.");
                    populateArticleTable();
                    clearFields();
                });

            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to update the article: " + e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    // Method to delete an article
    @FXML
    public void deleteArticle() {
        executorService.submit(() -> {
            try {
                Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
                if (selectedArticle == null) {
                    showAlert("Error", "Please select an article to delete.");
                    return;
                }

                boolean confirmDeletion = showConfirmationAlert("Delete Article", "Are you sure you want to delete this article?");
                if (!confirmDeletion) {
                    return;
                }

                // Check if the Deleted_Articles collection exists
                if (deletedArticlesCollection == null) {
                    showAlert("Error", "The Deleted_Articles collection does not exist.");
                    return;
                }

                // Log the deletion in Deleted_Articles collection
                String deletedAt = Instant.now()
                        .atZone(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ISO_INSTANT);
                Document deletedArticleLog = new Document("article_id", selectedArticle.getString("article_id"))
                        .append("article_name", selectedArticle.getString("article_name"))
                        .append("author", selectedArticle.getString("author"))
                        .append("published_date", selectedArticle.getString("published_date"))
                        .append("description", selectedArticle.getString("description"))
                        .append("content", selectedArticle.getString("content"))
                        .append("category", selectedArticle.getString("category"))
                        .append("deleted_at", deletedAt);

                deletedArticlesCollection.insertOne(deletedArticleLog);

                ObjectId articleId = selectedArticle.getObjectId("_id");
                articlesCollection.deleteOne(new Document("_id", articleId));

                Platform.runLater(() -> {
                    showAlert("Success", "Article deleted successfully.");
                    populateArticleTable();
                });

            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to delete the article: " + e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    // Helper method to show a simple alert
    private void showAlert(String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Helper method to show a confirmation alert
    private boolean showConfirmationAlert(String title, String content) {
        // For simplicity, returning true as if the user confirms the action.
        // You can add actual dialog logic here if needed (like a Yes/No dialog).
        return true;
    }

    // Helper method to clear fields
    private void clearFields() {
        articleNameField.clear();
        categoryField.clear();
        authorField.clear();
        manageDescription.clear();
        manageContent.clear();
        publishedDateField.clear();
    }

    // Helper method to populate the article table
    private void populateArticleTable() {
        try {
            List<Document> articles = articlesCollection.find().into(new ArrayList<>());

            // Assuming your TableView has columns for these fields
            articleTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("article_name"));
            articleTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("category"));
            articleTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("author"));
            articleTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("published_date"));

            articleTable.getItems().setAll(articles);

        } catch (Exception e) {
            showAlert("Error", "Failed to populate article table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void manageMainMenu(ActionEvent actionEvent) {
        executorService.submit(() -> {
            try {
                // Close the current window
                Stage stage = (Stage) articleNameField.getScene().getWindow();
                stage.close();

                // Open the main menu window
                FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_main_menu.fxml"));
                Stage mainMenuStage = new Stage();
                mainMenuStage.setScene(new Scene(loader.load()));
                mainMenuStage.setTitle("Main Menu");
                mainMenuStage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to open the Main Menu: " + e.getMessage());
            }
        });
    }
    @FXML
    public void manageExit(ActionEvent actionEvent) {
        executorService.submit(() -> {
            // Close the current window
            Stage stage = (Stage) articleNameField.getScene().getWindow();
            stage.close();
        });
    }
}
