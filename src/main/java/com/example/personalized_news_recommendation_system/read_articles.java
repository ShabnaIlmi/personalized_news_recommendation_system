package com.example.personalized_news_recommendation_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;

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

    public void setArticleDetails(Article article) {
        this.article = article;

        articleNameLabel.setText(article.getName());
        articlePublishedDateLabel.setText("Published: " + (article.getPublishedDate() != null ? article.getPublishedDate() : "N/A"));
        articleAuthorLabel.setText("Author: " + article.getAuthor());
        articleContentLabel.setText(article.getContent());

        // Log the "read" interaction
        logInteraction("read", false, false, null);
    }

    @FXML
    public void handleLike(ActionEvent actionEvent) {
        logInteraction("like", true, false, null);
        showAlert("Liked", "You liked this article!");
    }

    @FXML
    public void handleDislike(ActionEvent actionEvent) {
        logInteraction("dislike", false, true, null);
        showAlert("Disliked", "You disliked this article.");
    }

    @FXML
    public void handleBack(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recommended_articles.fxml"));
            Stage currentStage = (Stage) articleNameLabel.getScene().getWindow();
            currentStage.setScene(new Scene(loader.load()));
            recommended_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.populateRecommendedTable();
            controller.setUserDetails(currentUserId, currentSessionId);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Recommended Articles page.");
        }
    }

    @FXML
    public void handleExit(ActionEvent actionEvent) {
        Stage stage = (Stage) articleNameLabel.getScene().getWindow();
        stage.close();
    }

    private void logInteraction(String interactionType, boolean liked, boolean disliked, String skippedReason) {
        if (mongoClient == null || database == null || article == null) return;

        MongoCollection<Document> userInterfaceCollection = database.getCollection("UserInterface");

        Document interactionLog = new Document("userId", currentUserId)
                .append("sessionId", currentSessionId)
                .append("articleId", article.getId())
                .append("articleName", article.getName())
                .append("timestamp", Instant.now().toString())
                .append("interactionType", interactionType)
                .append("details", new Document("liked", liked)
                        .append("disliked", disliked)
                        .append("skippedReason", skippedReason));

        userInterfaceCollection.insertOne(interactionLog);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
