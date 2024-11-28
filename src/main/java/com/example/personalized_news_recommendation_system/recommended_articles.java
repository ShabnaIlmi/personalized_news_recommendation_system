package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class recommended_articles {

    @FXML
    private Button recommendedMainMenu, recommendedExit, viewArticle, getRecommendation;

    @FXML
    private TableView<Article> recommendedTable;

    @FXML
    private TableColumn<Article, String> articleNameColumn, categoryColumn, authorColumn, publishedDateColumn;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> articlesCollection;
    private String currentUserId;
    private String currentSessionId;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // List to store session interactions
    private List<Document> sessionInteractions = new ArrayList<>();

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            populateRecommendedTable(); // Populate table after collections are initialized
        }
    }

    public void setUserDetails(String userId, String sessionId) {
        this.currentUserId = userId;
        this.currentSessionId = sessionId;
    }

    public void setSessionInteractions(List<Document> sessionInteractions) {
        this.sessionInteractions = sessionInteractions;
    }

    // Populate the recommended articles table
    void populateRecommendedTable() {
        executorService.submit(() -> {
            try {
                if (articlesCollection == null) {
                    Platform.runLater(() -> showAlert("Error", "Articles collection is null.", Alert.AlertType.ERROR));
                    return;
                }

                List<Document> articles = articlesCollection.find().into(new ArrayList<>());
                if (articles.isEmpty()) {
                    Platform.runLater(() -> showAlert("Error", "No articles found in the database.", Alert.AlertType.ERROR));
                    return;
                }

                List<Article> articleList = convertDocumentsToArticles(articles);

                Platform.runLater(() -> {
                    recommendedTable.getItems().setAll(articleList);
                    System.out.println("Populated articles count: " + articleList.size()); // Debugging log
                });
            } catch (Exception e) {
                e.printStackTrace(); // Print the stack trace for debugging
                Platform.runLater(() -> showAlert("Error", "Failed to populate article table: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        });
    }

    private List<Article> convertDocumentsToArticles(List<Document> documents) {
        List<Article> articles = new ArrayList<>();
        for (Document doc : documents) {
            // Check if critical fields are missing
            if (doc.getString("article_id") == null || doc.getString("article_name") == null) {
                System.out.println("Warning: Missing article_id or article_name in document.");
                continue; // Skip this document if it's missing critical fields
            }

            articles.add(new Article(
                    doc.getString("article_id"),
                    doc.getString("article_name"),
                    doc.getString("category"),
                    doc.getString("author"),
                    doc.getString("published_date"),
                    doc.getString("description"),
                    doc.getString("content")
            ));
        }
        return articles;
    }

    // Handle the action when viewing an article
    @FXML
    public void viewArticle(ActionEvent actionEvent) {
        Article selectedArticle = recommendedTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert("Error", "No article selected.", Alert.AlertType.ERROR);
            return;
        }

        logInteraction(selectedArticle, "view");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            view_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserDetails(currentUserId, currentSessionId);
            controller.setArticleDetails(selectedArticle);

            controller.setSessionInteractions(sessionInteractions); // Pass session interactions
            currentStage.setScene(scene);
            currentStage.setTitle("View Article");
        } catch (IOException e) {
            showAlert("Error", "Failed to load article view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Log the user interaction with the article (view, like, etc.)
    private void logInteraction(Article article, String interactionType) {
        Document interaction = new Document("articleId", article.getId())
                .append("articleName", article.getName())
                .append("timestamp", Instant.now().toString())
                .append("interactionType", interactionType);
        sessionInteractions.add(interaction);  // Accumulate interactions in the session
    }

    @FXML
    public void recommendedMainMenu(ActionEvent actionEvent) {
        storeSessionInteractions();  // Save session data before navigating to the main menu
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_main_menu.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            user_main_menu controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserInfo(currentUserId, currentSessionId);

            currentStage.setScene(scene);
            currentStage.setTitle("User Main Menu");
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void recommendedExit(ActionEvent actionEvent) {
        storeSessionInteractions();  // Save session data before exiting
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

    // Store all session interactions in the database
    private void storeSessionInteractions() {
        if (mongoClient == null || database == null || sessionInteractions.isEmpty()) return;

        MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");

        Document sessionDocument = new Document("userId", currentUserId)
                .append("sessionId", currentSessionId)
                .append("interactions", sessionInteractions)
                .append("sessionEnd", Instant.now().toString());

        userPreferencesCollection.insertOne(sessionDocument);  // Store the entire session of interactions
        sessionInteractions.clear();  // Clear session interactions after saving
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        articleNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName())
        );
        categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory())
        );
        authorColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAuthor())
        );
        publishedDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPublishedDate().toString())
        );
    }

    public void getRecommendation(ActionEvent actionEvent) {
        // Implement recommendation logic here if needed
    }
}
