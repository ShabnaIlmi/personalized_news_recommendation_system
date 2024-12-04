package com.example.personalized_news_recommendation_system.Controller.UserController;

import com.example.personalized_news_recommendation_system.Model.Article;
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

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
    private MongoCollection<Document> userCollection;

    private String currentUserId;
    private String currentSessionId;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private List<Document> sessionInteractions = new ArrayList<>();

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            this.userCollection = database.getCollection("User");
            populateRecommendedTable();
        }
    }

    public void setUserDetails(String userId, String sessionId) {
        this.currentUserId = userId;
        this.currentSessionId = sessionId;
    }

    public void setSessionInteractions(List<Document> sessionInteractions) {
        this.sessionInteractions = sessionInteractions;
    }

    private void populateRecommendedTable() {
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

                Platform.runLater(() -> recommendedTable.getItems().setAll(articleList));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to populate article table: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        });
    }

    private List<Article> convertDocumentsToArticles(List<Document> documents) {
        List<Article> articles = new ArrayList<>();
        for (Document doc : documents) {
            if (doc.getString("article_id") == null || doc.getString("article_name") == null) {
                continue;
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

    @FXML
    public void viewArticle(ActionEvent actionEvent) {
        Article selectedArticle = recommendedTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert("Error", "No article selected.", Alert.AlertType.ERROR);
            return;
        }

        logInteraction(selectedArticle, "view");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/view_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            view_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserDetails(currentUserId, currentSessionId);
            controller.setArticleDetails(selectedArticle);

            controller.setSessionInteractions(sessionInteractions);
            currentStage.setScene(scene);
            currentStage.setTitle("View Article");
        } catch (IOException e) {
            showAlert("Error", "Failed to load article view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void logInteraction(Article article, String interactionType) {
        Document interaction = new Document("article_id", article.getId())
                .append("article_name", article.getName())
                .append("category", article.getCategory())
                .append("timestamp", Instant.now().toString())
                .append("interactionType", interactionType);
        sessionInteractions.add(interaction);
    }

    @FXML
    public void recommendedMainMenu(ActionEvent actionEvent) {
        storeSessionInteractions();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/user_main_menu.fxml"));
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

    private void storeSessionInteractions() {
        if (mongoClient == null || database == null || sessionInteractions.isEmpty()) return;

        MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");

        Document sessionDocument = new Document("user_id", currentUserId)
                .append("session_id", currentSessionId)
                .append("interactions", sessionInteractions)
                .append("sessionEnd", Instant.now().toString());

        userPreferencesCollection.insertOne(sessionDocument);
        sessionInteractions.clear();
    }

    private List<Document> getUserPreferences(String userId) {
        MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");

        List<Document> sessions = userPreferencesCollection.find(new Document("user_id", userId)).into(new ArrayList<>());
        List<Document> allInteractions = new ArrayList<>();
        for (Document session : sessions) {
            List<Document> interactions = (List<Document>) session.get("interactions");
            if (interactions != null) {
                allInteractions.addAll(interactions);
            }
        }

        return allInteractions;
    }

    private Map<String, Integer> analyzeInteractions(List<Document> allInteractions) {
        Map<String, Integer> categoryScoreMap = new HashMap<>();

        if (allInteractions == null || allInteractions.isEmpty()) {
            Document userDocument = userCollection.find(eq("username", currentUserId)).first();
            if (userDocument != null) {
                List<String> preferredCategories = userDocument.getList("categories", String.class);

                if (preferredCategories != null && !preferredCategories.isEmpty()) {
                    return categoryScoreMap;
                }
            }
        }

        for (Document interaction : allInteractions) {
            String category = interaction.getString("category");
            String interactionType = interaction.getString("interactionType");

            if (category == null || interactionType == null) continue;

            switch (interactionType) {
                case "like":
                    categoryScoreMap.put(category, categoryScoreMap.getOrDefault(category, 0) + 3);
                    break;
                case "view":
                    categoryScoreMap.put(category, categoryScoreMap.getOrDefault(category, 0) + 1);
                    break;
                case "skip":
                    categoryScoreMap.put(category, categoryScoreMap.getOrDefault(category, 0) - 1);
                    break;
                case "dislike":
                    categoryScoreMap.put(category, categoryScoreMap.getOrDefault(category, 0) - 3);
                    break;
            }
        }

        return categoryScoreMap;
    }

    @FXML
    public void getRecommendation(ActionEvent actionEvent) {
        List<Document> allInteractions = getUserPreferences(currentUserId);

        Map<String, Integer> categoryScores = analyzeInteractions(allInteractions);

        List<String> recommendedCategories = categoryScores.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Document> recommendedArticles = new ArrayList<>();

        if (allInteractions.isEmpty()) {
            Document userDocument = userCollection.find(eq("username", currentUserId)).first();
            if (userDocument != null) {
                List<String> preferredCategories = userDocument.getList("categories", String.class);

                if (preferredCategories != null && !preferredCategories.isEmpty()) {
                    recommendedArticles = articlesCollection.find(in("category", preferredCategories)).into(new ArrayList<>());
                }
            }

            if (recommendedArticles.isEmpty()) {
                showAlert("Recommendation", "No recommendations available based on your preferences.", Alert.AlertType.INFORMATION);
            } else {
                List<Article> articleList = convertDocumentsToArticles(recommendedArticles);
                recommendedTable.getItems().setAll(articleList);
                showAlert("Recommendation", "Recommendations generated based on your user preferences.", Alert.AlertType.INFORMATION);
            }
        } else {
            recommendedArticles = articlesCollection.find(in("category", recommendedCategories)).into(new ArrayList<>());

            if (recommendedArticles.isEmpty()) {
                showAlert("Recommendation", "No recommendations available based on your recent interactions.", Alert.AlertType.INFORMATION);
            } else {
                List<Article> articleList = convertDocumentsToArticles(recommendedArticles);
                recommendedTable.getItems().setAll(articleList);
                showAlert("Recommendation", "Recommendations generated based on your recent interactions.", Alert.AlertType.INFORMATION);
            }
        }
    }

    @FXML
    public void recommendedExit(ActionEvent actionEvent) {
        storeSessionInteractions();
        executorService.shutdown();
        Platform.exit();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
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
}

