package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.time.LocalDate;

public class add_article {
    @FXML
    public Button addMainMenu;
    @FXML
    public Button exitArticle;
    @FXML
    private TextField articleNameField;
    @FXML
    private TextField authorField;
    @FXML
    private DatePicker publishedDatePicker;
    @FXML
    private TextArea contentArea;
    @FXML
    private Button submitArticleButton;

    private MongoDatabase database;

    // OpenNLP objects for tokenization, POS tagging, and NER
    private OpenNLPExample openNLPExample;

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    // Initialize OpenNLP objects
    public void initOpenNLP() throws Exception {
        openNLPExample = new OpenNLPExample();
    }

    // Method to handle the submit button click
    @FXML
    public void submitArticle(ActionEvent event) {
        // Get values from the fields
        String articleName = articleNameField.getText();
        String author = authorField.getText();
        String content = contentArea.getText();
        LocalDate publishedDate = publishedDatePicker.getValue();

        // Validate input fields
        if (articleName == null || articleName.trim().isEmpty()) {
            showAlert("Input Error", "Article name cannot be empty.");
            return;
        }
        if (author == null || author.trim().isEmpty()) {
            showAlert("Input Error", "Author name cannot be empty.");
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            showAlert("Input Error", "Content cannot be empty.");
            return;
        }
        if (publishedDate == null) {
            showAlert("Input Error", "Please select a published date.");
            return;
        }

        // Categorize the article using OpenNLP (integration will go here)
        String category = categorizeArticle(content);

        // Create a new article document
        Document article = new Document("article_name", articleName)
                .append("author", author)
                .append("published_date", publishedDate.toString())
                .append("content", content)
                .append("category", category);

        // Insert the article into the MongoDB "Articles" collection
        MongoCollection<Document> articlesCollection = database.getCollection("Articles");
        articlesCollection.insertOne(article);

        // Show a success message
        showAlert("Success", "Article submitted successfully!");

        // Clear the form fields
        articleNameField.clear();
        authorField.clear();
        contentArea.clear();
        publishedDatePicker.setValue(null);
    }

    // Method to categorize article using OpenNLP models
    private String categorizeArticle(String content) {
        try {
            // Tokenize the content
            String[] tokens = openNLPExample.tokenizer.tokenize(content);

            // Perform NER to identify entities
            Span[] nameSpans = openNLPExample.nameFinder.find(tokens);

            // If we found any entities, we can categorize based on them
            if (nameSpans.length > 0) {
                StringBuilder categories = new StringBuilder();
                for (Span span : nameSpans) {
                    categories.append(tokens[span.getStart()]).append(", "); // Collect names/entities
                }
                return "Entities found: " + categories.toString(); // Use found entities as category
            }

            // If no entities found, categorize as general topic
            return "General";

        } catch (Exception e) {
            // In case of error, categorize as Uncategorized
            e.printStackTrace();
            showAlert("Error", "Error during categorization. Using default category.");
            return "Uncategorized";
        }
    }

    // Method to display an alert with a custom title and message
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigate to the Main Menu page
    @FXML
    public void addMainMenu(ActionEvent actionEvent) {
        try {
            // Load the main menu scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adminstrator_main_menu.fxml"));
            Scene mainMenuScene = new Scene(loader.load());

            // Get the current stage and set the main menu scene
            Stage currentStage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(mainMenuScene);
            currentStage.setTitle("Administrator Main Menu");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the Administrator Main Menu.");
            e.printStackTrace();
        }
    }

    // Close the current article page
    @FXML
    public void exitArticle(ActionEvent actionEvent) {
        // Close the current stage (article page)
        Stage currentStage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
