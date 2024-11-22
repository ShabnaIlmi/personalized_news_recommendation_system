package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class add_article {

    @FXML
    private TextField articleID;
    @FXML
    private TextField articleNameField;
    @FXML
    private TextField authorField;
    @FXML
    private DatePicker publishedDatePicker;
    @FXML
    private TextArea description;
    @FXML
    private TextArea contentArea;

    private MongoClient mongoClient;
    private MongoCollection<Document> articlesCollection;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    // Set the MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in Add Article.");
    }

    // Set the MongoDatabase and initialize the articles collection
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            System.out.println("Articles collection initialized: " + articlesCollection.getNamespace());
        } else {
            System.err.println("Database is null. Cannot initialize articles collection.");
        }
    }

    // Handle the submit button action
    @FXML
    public void submitArticle() {
        executorService.submit(() -> {
            try {
                // Ensure the collection is initialized
                if (articlesCollection == null) {
                    throw new IllegalStateException("Articles collection not initialized.");
                }

                // Get data from the input fields
                String id = articleID.getText();
                String title = articleNameField.getText();
                String author = authorField.getText();
                String publishedDate = (publishedDatePicker.getValue() != null) ? publishedDatePicker.getValue().toString() : "";
                String articleDescription = description.getText();
                String content = contentArea.getText();
                String category = "General"; // Default category or retrieve from UI

                // Validate input fields
                if (id.isEmpty() || title.isEmpty() || author.isEmpty() || content.isEmpty() || publishedDate.isEmpty() || articleDescription.isEmpty()) {
                    showAlert("Validation Error", "Please fill all fields before submitting.");
                    return;
                }

                // Check if an article with the same articleID already exists
                Document existingArticle = articlesCollection.find(new Document("articleID", id)).first();
                if (existingArticle != null) {
                    showAlert("Validation Error", "An article with the same Article ID already exists. Please use a unique ID.");
                    return;
                }

                // Get the current timestamp in ISO 8601 format
                String articleAddedTime = Instant.now()
                        .atZone(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ISO_INSTANT);

                // Create the MongoDB document
                Document article = new Document("articleID", id)
                        .append("articleName", title)
                        .append("author", author)
                        .append("publishedDate", publishedDate)
                        .append("article_added_time", articleAddedTime)
                        .append("description", articleDescription)
                        .append("content", content)
                        .append("category", category);

                // Insert the document into the database
                articlesCollection.insertOne(article);

                // Show success message
                Platform.runLater(() -> showAlert("Success", "Article added successfully."));

                // Clear input fields after submission
                Platform.runLater(this::clearFields);
            } catch (Exception e) {
                // Handle any errors
                Platform.runLater(() -> showAlert("Error", "Failed to submit the article: " + e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    // Helper method to show an alert dialog
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper method to clear input fields
    private void clearFields() {
        articleID.clear();
        articleNameField.clear();
        authorField.clear();
        description.clear();
        contentArea.clear();
        publishedDatePicker.setValue(null);
    }

    // Handle the "Main Menu" button action
    @FXML
    public void addMainMenu() {
        // Use Platform.runLater to execute UI changes on the JavaFX main thread
        Platform.runLater(() -> {
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

    // Handle the "Exit" button action
    @FXML
    public void exitArticle() {
        // Use Platform.runLater to execute UI changes on the JavaFX main thread
        Platform.runLater(() -> {
            // Close the current window
            Stage stage = (Stage) articleNameField.getScene().getWindow();
            stage.close();
        });
    }
}
