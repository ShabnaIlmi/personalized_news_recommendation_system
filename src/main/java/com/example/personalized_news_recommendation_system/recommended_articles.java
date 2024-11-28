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
import java.util.*;
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
            populateRecommendedTable();
        }
    }

    public void setUserDetails(String userId, String sessionId) {
        this.currentUserId = userId;
        this.currentSessionId = sessionId;
        System.out.println("User details set: userId = " + userId + ", sessionId = " + sessionId);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view_articles.fxml"));
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
        Document interaction = new Document("articleId", article.getId())
                .append("articleName", article.getName())
                .append("timestamp", Instant.now().toString())
                .append("interactionType", interactionType);
        sessionInteractions.add(interaction);
        System.out.println("Logged interaction: " + interaction);
    }

    @FXML
    public void recommendedMainMenu(ActionEvent actionEvent) {
        storeSessionInteractions();
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
        storeSessionInteractions();
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

    private void storeSessionInteractions() {
        if (mongoClient == null || database == null || sessionInteractions.isEmpty()) return;

        MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");

        Document sessionDocument = new Document("userId", currentUserId)
                .append("sessionId", currentSessionId)
                .append("interactions", sessionInteractions)
                .append("sessionEnd", Instant.now().toString());

        userPreferencesCollection.insertOne(sessionDocument);
        System.out.println("Stored session interactions: " + sessionDocument);
        sessionInteractions.clear();
    }

    private List<Document> getUserPreferences(String userId) {
        MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");

        List<Document> sessions = userPreferencesCollection.find(new Document("userId", userId)).into(new ArrayList<>());

        List<Document> allInteractions = new ArrayList<>();
        for (Document session : sessions) {
            List<Document> interactions = (List<Document>) session.get("interactions");
            if (interactions != null) {
                allInteractions.addAll(interactions);
            }
        }

        System.out.println("Aggregated interactions for userId " + userId + ": " + allInteractions);
        return allInteractions;
    }

    private List<String> getUserCategories(String userId) {
        if (database == null) {
            showAlert("Error", "Database connection is not available.", Alert.AlertType.ERROR);
            return new ArrayList<>();
        }

        MongoCollection<Document> userCollection = database.getCollection("User");
        Document userDocument = userCollection.find(new Document("userId", userId)).first();

        if (userDocument == null || !userDocument.containsKey("preferredCategories")) {
            return new ArrayList<>();
        }

        List<String> categories = (List<String>) userDocument.get("preferredCategories");
        return categories != null ? categories : new ArrayList<>();
    }

    private List<Article> fetchArticlesByCategories(List<String> categories) {
        List<Article> articles = new ArrayList<>();
        for (String category : categories) {
            articles.addAll(fetchArticlesByCategory(category));
        }
        return articles;
    }

    private List<Article> fetchArticlesByCategory(String category) {
        List<Document> documents = articlesCollection.find(new Document("category", category)).into(new ArrayList<>());
        return convertDocumentsToArticles(documents);
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

    @FXML
    public void getRecommendation(ActionEvent actionEvent) {
        recommendedTable.getItems().clear();

        List<Document> userPreferencesList = getUserPreferences(currentUserId);

        if (userPreferencesList.isEmpty()) {
            List<String> userCategories = getUserCategories(currentUserId);

            if (userCategories.isEmpty()) {
                showAlert("Info", "No categories found for the user. Unable to fetch recommendations.", Alert.AlertType.INFORMATION);
                return;
            }

            List<Article> articles = fetchArticlesByCategories(userCategories);

            if (articles.isEmpty()) {
                showAlert("Info", "No articles found for your preferred categories.", Alert.AlertType.INFORMATION);
            } else {
                recommendedTable.getItems().setAll(articles);
                showAlert("Info", "Recommendations based on your preferred categories.", Alert.AlertType.INFORMATION);
            }
            return;
        }

        Map<String, Integer> categoryScoreMap = new HashMap<>();

        for (Document preference : userPreferencesList) {
            List<Document> interactions = (List<Document>) preference.get("interactions");
            if (interactions != null) {
                System.out.println("Interactions found for user preference: " + preference);
                for (Document interaction : interactions) {
                    String category = interaction.getString("category");
                    String interactionType = interaction.getString("interactionType");

                    // Check for null or invalid data
                    if (category == null || interactionType == null) {
                        System.out.println("Skipping interaction with missing data: " + interaction);
                        continue;  // Skip invalid interaction
                    }

                    // Update category score based on interaction type
                    switch (interactionType) {
                        case "liked":
                            categoryScoreMap.put(category, categoryScoreMap.getOrDefault(category, 0) + 3);
                            break;
                        case "viewed":
                            categoryScoreMap.put(category, categoryScoreMap.getOrDefault(category, 0) + 2);
                            break;
                        case "skipped":
                            categoryScoreMap.put(category, categoryScoreMap.getOrDefault(category, 0) - 1);
                            break;
                        case "disliked":
                            categoryScoreMap.put(category, categoryScoreMap.getOrDefault(category, 0) - 3);
                            break;
                        default:
                            System.out.println("Unknown interaction type: " + interactionType);
                    }
                }
            }
        }

        System.out.println("Final categoryScoreMap: " + categoryScoreMap);
        if (categoryScoreMap.isEmpty()) {
            System.out.println("No meaningful interaction data found. Fallback to popular articles.");
            List<Article> popularArticles = fetchArticlesByCategories(List.of("Technology", "Science", "Health"));
            if (popularArticles.isEmpty()) {
                showAlert("Info", "No popular articles available.", Alert.AlertType.INFORMATION);
            } else {
                recommendedTable.getItems().setAll(popularArticles);
                showAlert("Info", "Recommendations based on popular categories.", Alert.AlertType.INFORMATION);
            }
            return;
        }


        List<Article> recommendedArticles = new ArrayList<>();

        categoryScoreMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()))
                .forEach(entry -> recommendedArticles.addAll(fetchArticlesByCategory(entry.getKey())));

        if (recommendedArticles.isEmpty()) {
            showAlert("Info", "No articles found based on your preferences.", Alert.AlertType.INFORMATION);
        } else {
            recommendedTable.getItems().setAll(recommendedArticles);
            showAlert("Info", "Recommendations based on your preferences.", Alert.AlertType.INFORMATION);
        }
    }
}
