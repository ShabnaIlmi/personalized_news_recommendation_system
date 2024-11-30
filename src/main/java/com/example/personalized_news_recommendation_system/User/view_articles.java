package com.example.personalized_news_recommendation_system.User;

import com.example.personalized_news_recommendation_system.Model.Article;
import com.mongodb.client.MongoCollection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class view_articles {

    @FXML
    private Text articleName, articleAuthorValue, articleCategoryValue, articlePublishedDateValue, descriptionValue;

    private Article article;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private String currentUserId;
    private String currentSessionId;

    // Session interactions passed from the previous view
    private List<Document> sessionInteractions;

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

    public void setSessionInteractions(List<Document> sessionInteractions) {
        this.sessionInteractions = sessionInteractions;
    }

    public void setArticleDetails(Article article) {
        this.article = article;
        articleName.setText(article.getName());
        articleAuthorValue.setText(article.getAuthor());
        articleCategoryValue.setText(article.getCategory());
        articlePublishedDateValue.setText(article.getPublishedDate().toString());
        descriptionValue.setText(article.getDescription());
    }

    @FXML
    private void readArticle(ActionEvent actionEvent) {
        try {
            // Log the "read" interaction for the article
            logInteraction(article, "read");

            // Load the FXML for the read_articles page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/read_articles.fxml"));

            // Load the scene from the FXML file
            Scene scene = new Scene(loader.load());

            // Get the current stage (window)
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Get the controller of the read_articles page
            read_articles controller = loader.getController();

            // Pass the article details to the controller of the read_articles page
            controller.setArticleDetails(article);  // Pass the article details to the new page

            // Pass other necessary data (e.g., user details, session interactions)
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserDetails(currentUserId, currentSessionId);
            controller.setSessionInteractions(sessionInteractions);  // Pass session interactions

            // Set the scene to the current stage (this changes the window content)
            currentStage.setScene(scene);
            currentStage.setTitle("Read Article");

        } catch (IOException e) {
            // If there was an error loading the read_articles page, show an alert
            showAlert("Error", "Failed to load read article page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void logInteraction(Article article, String interactionType) {
        Document interaction = new Document("article_id", article.getId())
                .append("article_name", article.getName())
                .append("category", article.getCategory())
                .append("timestamp", Instant.now().toString())
                .append("interactionType", interactionType);
        sessionInteractions.add(interaction);
        System.out.println("Logged interaction: " + interaction);
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/recommended_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            recommended_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserDetails(currentUserId, currentSessionId);
            controller.setSessionInteractions(sessionInteractions);  // Pass session interactions
            currentStage.setScene(scene);
            currentStage.setTitle("Recommended Articles");
        } catch (IOException e) {
            showAlert("Error", "Failed to load recommended articles: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void exitApplication(ActionEvent actionEvent) {
        // Store session interactions before exiting
        storeSessionInteractions();
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();  // Close the application
    }

    @FXML
    public void skipArticle(ActionEvent actionEvent) {
        // Log skip interaction and navigate to the recommended articles page
        logInteraction(article, "skip");
        goBack(actionEvent);  // Go back to the recommended articles page after skipping
    }

    private void storeSessionInteractions() {
        if (mongoClient == null || database == null || sessionInteractions.isEmpty()) return;

        MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");

        // Create a document to store all session interactions
        Document sessionDocument = new Document("user_id", currentUserId)
                .append("session_id", currentSessionId)
                .append("interactions", sessionInteractions)
                .append("sessionEnd", Instant.now().toString());

        // Insert the session document into the MongoDB collection
        userPreferencesCollection.insertOne(sessionDocument);
        sessionInteractions.clear();  // Clear the list after storing
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
