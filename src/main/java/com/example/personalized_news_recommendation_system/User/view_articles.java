package com.example.personalized_news_recommendation_system.User;

import com.example.personalized_news_recommendation_system.Model.Article;
import com.mongodb.client.MongoCollection;
import javafx.application.Platform;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class view_articles {

    @FXML
    private Text articleName, articleAuthorValue, articleCategoryValue, articlePublishedDateValue, descriptionValue;

    private Article article;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private String currentUserId;
    private String currentSessionId;

    private List<Document> sessionInteractions = Collections.synchronizedList(new java.util.ArrayList<>());

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

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
        synchronized (this.sessionInteractions) {
            this.sessionInteractions.clear();
            this.sessionInteractions.addAll(sessionInteractions);
        }
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
            logInteraction(article, "read");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/read_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            read_articles controller = loader.getController();
            controller.setArticleDetails(article);
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserDetails(currentUserId, currentSessionId);
            controller.setSessionInteractions(sessionInteractions);

            currentStage.setScene(scene);
            currentStage.setTitle("Read Article");
        } catch (IOException e) {
            showAlert("Error", "Failed to load read article page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private synchronized void logInteraction(Article article, String interactionType) {
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
            controller.setSessionInteractions(sessionInteractions);

            currentStage.setScene(scene);
            currentStage.setTitle("Recommended Articles");
        } catch (IOException e) {
            showAlert("Error", "Failed to load recommended articles: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void exitApplication(ActionEvent actionEvent) {
        storeSessionInteractions();
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
        shutdownExecutor();
    }

    @FXML
    public void skipArticle(ActionEvent actionEvent) {
        logInteraction(article, "skip");
        goBack(actionEvent);
    }

    private void storeSessionInteractions() {
        if (mongoClient == null || database == null || sessionInteractions.isEmpty()) return;

        executorService.submit(() -> {
            try {
                MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");

                Document sessionDocument = new Document("user_id", currentUserId)
                        .append("session_id", currentSessionId)
                        .append("interactions", sessionInteractions)
                        .append("sessionEnd", Instant.now().toString());

                userPreferencesCollection.insertOne(sessionDocument);

                synchronized (sessionInteractions) {
                    sessionInteractions.clear();
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to store session interactions: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        });
    }

    private void shutdownExecutor() {
        executorService.shutdown();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
