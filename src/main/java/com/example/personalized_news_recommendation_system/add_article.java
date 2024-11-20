package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

public class add_article {

    @FXML
    private TextField articleNameField;
    @FXML
    private TextField authorField;
    @FXML
    private DatePicker publishedDatePicker;
    @FXML
    private TextArea contentArea;

    private MongoClient mongoClient;
    private MongoCollection<Document> articlesCollection;

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
        try {
            // Ensure the collection is initialized
            if (articlesCollection == null) {
                throw new IllegalStateException("Articles collection not initialized.");
            }

            // Get data from the input fields
            String title = articleNameField.getText();
            String author = authorField.getText();
            String publishedDate = (publishedDatePicker.getValue() != null) ? publishedDatePicker.getValue().toString() : "";
            String content = contentArea.getText();
            String category = "General"; // Default category or retrieve from UI


            // Validate input fields
            if (title.isEmpty() || author.isEmpty() || content.isEmpty() || publishedDate.isEmpty()) {
                showAlert("Validation Error", "Please fill all fields before submitting.");
                return;
            }

            // Create the MongoDB document
            Document article = new Document("articleName", title)
                    .append("category", category)
                    .append("author", author)
                    .append("publishedDate", publishedDate)
                    .append("content", content);

            // Insert the document into the database
            articlesCollection.insertOne(article);

            // Show success message
            showAlert("Success", "Article added successfully.");

            // Clear input fields after submission
            clearFields();
        } catch (Exception e) {
            // Handle any errors
            showAlert("Error", "Failed to submit the article: " + e.getMessage());
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

    // Helper method to clear input fields
    private void clearFields() {
        articleNameField.clear();
        authorField.clear();
        contentArea.clear();
        publishedDatePicker.setValue(null);
    }

    // Handle the "Main Menu" button action
    @FXML
    public void addMainMenu() {
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
    public void exitArticle() {
        // Close the current window
        Stage stage = (Stage) articleNameField.getScene().getWindow();
        stage.close();
    }
}
