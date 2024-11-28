package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoCollection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class read_articles {

    @FXML
    private Label articleNameLabel;
    @FXML
    private Label articlePublishedDateLabel;
    @FXML
    private Label articleAuthorLabel;
    @FXML
    private Label articleContentLabel;

    private Article article;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private String currentUserId;
    private String currentSessionId;
    private List<Document> sessionInteractions;

    // Setters for dependency injection from previous controller
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    public void setUserDetails(String userId, String sessionId) {
        this.currentUserId = userId;
        this.currentSessionId = sessionId;
    }

    public void setSessionInteractions(List<Document> interactions) {
        this.sessionInteractions = interactions;
    }

    // Set the article details to be displayed
    public void setArticleDetails(Article article) {
        this.article = article;

        if (article != null) {
            articleNameLabel.setText(article.getName());
            articlePublishedDateLabel.setText("Published: " + (article.getPublishedDate() != null ? article.getPublishedDate() : "N/A"));
            articleAuthorLabel.setText("Author: " + article.getAuthor());
            articleContentLabel.setText(article.getContent());
        } else {
            showAlert("Error", "Article details are not available.", Alert.AlertType.ERROR);
        }
    }

    // Handle the like button click
    @FXML
    public void handleLike(ActionEvent actionEvent) {
        // Check if the user has previously disliked this article in the same session
        Optional<Document> existingDislike = sessionInteractions.stream()
                .filter(interaction -> interaction.getString("interactionType").equals("dislike") &&
                        interaction.getString("articleId").equals(article.getId()) &&
                        interaction.getString("sessionId").equals(currentSessionId))
                .findFirst();

        if (existingDislike.isPresent()) {
            // Remove the previous dislike interaction
            sessionInteractions.remove(existingDislike.get());
        }

        logInteraction("like", true, false);  // Log the "like" interaction
        storeSessionInteractions();  // Save interaction immediately
        showAlert("Liked", "You liked this article!", Alert.AlertType.INFORMATION);
    }

    // Handle the dislike button click
    @FXML
    public void handleDislike(ActionEvent actionEvent) {
        // Check if the user has previously liked this article in the same session
        Optional<Document> existingLike = sessionInteractions.stream()
                .filter(interaction -> interaction.getString("interactionType").equals("like") &&
                        interaction.getString("articleId").equals(article.getId()) &&
                        interaction.getString("sessionId").equals(currentSessionId))
                .findFirst();

        if (existingLike.isPresent()) {
            // Remove the previous like interaction
            sessionInteractions.remove(existingLike.get());
        }

        logInteraction("dislike", false, true);  // Log the "dislike" interaction
        storeSessionInteractions();  // Save interaction immediately
        showAlert("Disliked", "You disliked this article.", Alert.AlertType.INFORMATION);
    }

    // Log interaction with article (like/dislike)
    private void logInteraction(String interactionType, boolean liked, boolean disliked) {
        if (article != null) {
            Document interaction = new Document("articleId", article.getId())
                    .append("articleName", article.getName())
                    .append("sessionId", currentSessionId)
                    .append("interactionType", interactionType)
                    .append("timestamp", Instant.now().toString());
            sessionInteractions.add(interaction);  // Add the interaction to the session list
        }
    }

    // Store all interactions in the session to the MongoDB
    private void storeSessionInteractions() {
        if (mongoClient == null || database == null || sessionInteractions.isEmpty()) return;

        MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");

        // Create a document to store all session interactions
        Document sessionDocument = new Document("userId", currentUserId)
                .append("sessionId", currentSessionId)
                .append("interactions", sessionInteractions)
                .append("sessionEnd", Instant.now().toString());

        // Insert the session document into the MongoDB collection
        userPreferencesCollection.insertOne(sessionDocument);
        sessionInteractions.clear();  // Clear the list after storing
    }

    // Display alert with a specific message
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Handle the back button click to return to recommended articles
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        try {
            // Ensure the correct FXML file path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recommended_articles.fxml"));

            // Load the scene and controller for recommended articles page
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) articleNameLabel.getScene().getWindow();

            // Get the controller of the recommended_articles page
            recommended_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserDetails(currentUserId, currentSessionId);
            controller.setSessionInteractions(sessionInteractions); // Pass session interactions

            // Set the scene and title on the current stage
            currentStage.setScene(scene);
            currentStage.setTitle("Recommended Articles");

        } catch (Exception e) {
            // Handle any errors related to loading the scene
            showAlert("Error", "Failed to go back to recommended articles: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Handle the exit button click to close the current window
    @FXML
    public void handleExit(ActionEvent actionEvent) {
        storeSessionInteractions();  // Save session data before exiting
        Stage currentStage = (Stage) articleNameLabel.getScene().getWindow();
        currentStage.close();  // Close the window
    }
}
