package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
    private Button manageMainMenu;
    @FXML
    private Button manageExit;
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
    private TextArea manageDescription;
    @FXML
    private TextArea manageContent;
    @FXML
    private TextField publishedDateField;

    private MongoClient mongoClient;
    private MongoCollection<Document> articlesCollection;
    private MongoCollection<Document> updatedArticlesCollection;
    private MongoCollection<Document> deletedArticlesCollection;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

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
        }
    }

    // Method to populate the article table
    private void populateArticleTable() {
        Platform.runLater(() -> {
            try {
                if (articlesCollection == null) {
                    showAlert("Error", "Articles collection is not initialized.");
                    return;
                }

                List<Document> articles = articlesCollection.find().into(new ArrayList<>());
                if (articles.isEmpty()) {
                    showAlert("Information", "No articles found in the database.");
                }

                // Bind columns to data
                articleIDColumn.setCellValueFactory(cellData -> {
                    Document doc = cellData.getValue();
                    return new SimpleStringProperty(doc.getObjectId("article_id").toString());
                });
                articleNameColumn.setCellValueFactory(cellData -> {
                    Document doc = cellData.getValue();
                    return new SimpleStringProperty(doc.getString("article_name"));
                });
                categoryColumn.setCellValueFactory(cellData -> {
                    Document doc = cellData.getValue();
                    return new SimpleStringProperty(doc.getString("category"));
                });
                authorColumn.setCellValueFactory(cellData -> {
                    Document doc = cellData.getValue();
                    return new SimpleStringProperty(doc.getString("author"));
                });
                dateColumn.setCellValueFactory(cellData -> {
                    Document doc = cellData.getValue();
                    return new SimpleStringProperty(doc.getString("published_date"));
                });

                // Set data to the table
                articleTable.getItems().setAll(articles);

            } catch (Exception e) {
                showAlert("Error", "Failed to populate article table: " + e.getMessage());
                e.printStackTrace();
            }
        });
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

                Document updatedArticle = new Document("article_name", articleName)
                        .append("category", category)
                        .append("author", author)
                        .append("published_date", publishedDate)
                        .append("description", description)
                        .append("content", content);

                ObjectId articleId = selectedArticle.getObjectId("_id");
                articlesCollection.updateOne(new Document("_id", articleId),
                        new Document("$set", updatedArticle));

                String updatedAt = Instant.now()
                        .atZone(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ISO_INSTANT);
                Document updateLog = new Document("article_id", articleId.toString())
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

                String deletedAt = Instant.now()
                        .atZone(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ISO_INSTANT);
                Document deletedArticleLog = new Document("article_id", selectedArticle.getObjectId("_id").toString())
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

    // Helper method to show an alert
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Helper method to show a confirmation alert
    private boolean showConfirmationAlert(String title, String content) {
        return true; // Simulated confirmation for simplicity
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

    // Main menu navigation
    @FXML
    public void manageMainMenu(ActionEvent actionEvent) {
        executorService.submit(() -> {
            try {
                Stage stage = (Stage) articleNameField.getScene().getWindow();
                stage.close();

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

    // Exit application
    @FXML
    public void manageExit(ActionEvent actionEvent) {
        executorService.submit(() -> {
            Stage stage = (Stage) articleNameField.getScene().getWindow();
            stage.close();
        });
    }

    @FXML
    public void initialize() {
        populateArticleTable();
    }
}
