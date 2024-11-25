package com.example.personalized_news_recommendation_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;

public class view_articles {

    @FXML
    private Text articleName;
    @FXML
    private Text articleAuthorValue;
    @FXML
    private Text articleCategoryValue;
    @FXML
    private Text articlePublishedDateValue;
    @FXML
    private Text descriptionValue;

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

        if (article != null) {
            articleName.setText(article.getName() != null ? article.getName() : "N/A");
            articleAuthorValue.setText(article.getAuthor() != null ? article.getAuthor() : "N/A");
            articleCategoryValue.setText(article.getCategory() != null ? article.getCategory() : "N/A");
            articlePublishedDateValue.setText(
                    article.getPublishedDate() != null ? article.getPublishedDate().toString() : "N/A"
            );
            descriptionValue.setText(article.getDescription() != null ? article.getDescription() : "N/A");

            // Log the "view" interaction
            logInteraction("view", false, false, null);
        } else {
            showAlert("No Article Found", "Article details are unavailable.");
        }
    }

    @FXML
    private void skipArticle(ActionEvent actionEvent) {
        logInteraction("skip", false, false, "User skipped the article");
        navigateToRecommendedArticles();
    }

    @FXML
    private void goBack(ActionEvent actionEvent) {
        navigateToRecommendedArticles();
    }

    @FXML
    private void readArticle(ActionEvent actionEvent) {
        // Log the "read" interaction
        logInteraction("read", false, false, null);
        navigateToArticleReadingPage();
    }

    private void logInteraction(String interactionType, boolean liked, boolean disliked, String skippedReason) {
        if (mongoClient == null || database == null || article == null) return;

        MongoCollection<Document> userInterfaceCollection = database.getCollection("User_Preferences");

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

    private void navigateToArticleReadingPage() {
        try {
            // Load the view_articles page (the full article view)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("read_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) articleName.getScene().getWindow();

            // Set up the controller with the necessary data
            read_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserDetails(currentUserId, currentSessionId);
            controller.setArticleDetails(article);  // Pass the article details to be shown

            currentStage.setScene(scene);
            currentStage.setTitle("Read Article");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the article details page: " + e.getMessage());
        }
    }

    private void navigateToRecommendedArticles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recommended_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) articleName.getScene().getWindow();

            recommended_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.populateRecommendedTable();
            controller.setUserDetails(currentUserId, currentSessionId);

            currentStage.setScene(scene);
            currentStage.setTitle("Recommended Articles");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Recommended Articles page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void exitApplication(ActionEvent actionEvent) {
        // Get the current stage (window) from the button triggering the action
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        // Close the stage (window)
        currentStage.close();
    }
}

