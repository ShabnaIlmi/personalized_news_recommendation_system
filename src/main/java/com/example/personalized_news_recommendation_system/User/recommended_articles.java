package com.example.personalized_news_recommendation_system.User;

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

    // Declare the MongoCollection objects for articles and user preferences
    private MongoCollection<Document> articlesCollection;
    private MongoCollection<Document> userCollection;

    private String currentUserId;
    private String currentSessionId;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private List<Document> sessionInteractions = new ArrayList<>();

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for MongoDatabase and initializing collections
    public void setDatabase(MongoDatabase database) {
        this.database = database;
        if (database != null) {
            // Initialize the collections
            this.articlesCollection = database.getCollection("Articles"); // Collection for articles
            this.userCollection = database.getCollection("User");     // Collection for user preferences (ensure this collection exists in your DB)
            populateRecommendedTable();
        }
    }

    public void setUserDetails(String userId, String sessionId) {
        this.currentUserId = userId;
        this.currentSessionId = sessionId;
        System.out.println("User details set: user_id = " + userId + ", session_id = " + sessionId);
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
        System.out.println("Logged interaction: " + interaction);
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
        System.out.println("Stored session interactions: " + sessionDocument);
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

        System.out.println("Aggregated interactions for user_id " + userId + ": " + allInteractions);
        return allInteractions;
    }

    private Map<String, Integer> analyzeInteractions(List<Document> allInteractions) {
        Map<String, Integer> categoryScoreMap = new HashMap<>();

        // If no interactions are available, recommend articles based on user's preferred categories
        if (allInteractions == null || allInteractions.isEmpty()) {
            Document userDocument = userCollection.find(eq("username", currentUserId)).first();
            if (userDocument != null) {
                List<String> preferredCategories = userDocument.getList("categories", String.class);

                if (preferredCategories != null && !preferredCategories.isEmpty()) {
                    // Fetch articles from all preferred categories
                    for (String category : preferredCategories) {
                        // Query the articles collection for articles in the current category
                        List<Document> articles = articlesCollection.find(eq("category", category)).into(new ArrayList<>());
                        System.out.println("Recommended articles in category '" + category + "':");
                        for (Document article : articles) {
                            System.out.println(article.getString("article_name"));
                        }
                    }
                    return categoryScoreMap; // Early return if we are recommending articles
                }
            }
        }

        // Process all interactions to calculate category scores
        for (Document interaction : allInteractions) {
            String category = interaction.getString("category");
            String interactionType = interaction.getString("interactionType");

            // Validate data
            if (category == null || interactionType == null) continue;

            // Assign scores based on interaction type
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

        // Debugging output
        System.out.println("Category Score Map: ");
        categoryScoreMap.forEach((key, value) -> System.out.println(key + ": " + value));


        // Output the category scores for debugging
        System.out.println("Category Score Map: ");
        categoryScoreMap.forEach((key, value) -> System.out.println(key + ": " + value));

        return categoryScoreMap;
    }

    @FXML
    public void getRecommendation(ActionEvent actionEvent) {
        // Fetch user preferences and interactions
        List<Document> allInteractions = getUserPreferences(currentUserId);

        // Analyze interactions to get category scores
        Map<String, Integer> categoryScores = analyzeInteractions(allInteractions);

        // Get top categories for recommendations (only if interactions exist)
        List<String> recommendedCategories = categoryScores.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue()) // Sort by score (descending)
                .limit(3) // Get top 3 categories
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Document> recommendedArticles = new ArrayList<>();

        if (allInteractions.isEmpty()) {
            // If there are no interactions, fetch articles based on the user's preferred categories
            Document userDocument = userCollection.find(eq("username", currentUserId)).first();
            if (userDocument != null) {
                List<String> preferredCategories = userDocument.getList("categories", String.class);

                // Fetch articles for the user's preferred categories
                if (preferredCategories != null && !preferredCategories.isEmpty()) {
                    recommendedArticles = articlesCollection.find(in("category", preferredCategories)).into(new ArrayList<>());
                }
            }

            if (recommendedArticles.isEmpty()) {
                showAlert("Recommendation", "No recommendations available based on your preferences.", Alert.AlertType.INFORMATION);
            } else {
                List<Article> articleList = convertDocumentsToArticles(recommendedArticles);
                recommendedTable.getItems().setAll(articleList); // Display articles in the table
                showAlert("Recommendation", "Articles recommended based on your preferred categories.", Alert.AlertType.INFORMATION);
            }
        } else {
            // If there are interactions, fetch articles from the recommended categories
            recommendedArticles = articlesCollection.find(in("category", recommendedCategories))
                    .into(new ArrayList<>());

            if (recommendedArticles.isEmpty()) {
                showAlert("Recommendation", "No recommendations available.", Alert.AlertType.INFORMATION);
            } else {
                List<Article> articleList = convertDocumentsToArticles(recommendedArticles);
                recommendedTable.getItems().setAll(articleList); // Display in the table
                showAlert("Recommendation", "Articles recommended based on your interactions.", Alert.AlertType.INFORMATION);
            }
        }
    }

    // Helper method to display alerts
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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

    // Shutdown ExecutorService during application exit
    public void recommendedExit(ActionEvent actionEvent) {
        storeSessionInteractions();
        executorService.shutdown(); // Shutdown the executor service
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

}
