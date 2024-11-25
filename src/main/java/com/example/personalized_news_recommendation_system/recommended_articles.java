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

    // Set MongoClient and Database
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        if (mongoClient != null) {
            System.out.println("MongoClient successfully set in Recommended Articles.");
        } else {
            System.err.println("MongoClient is null in Recommended Articles.");
        }
    }

    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            System.out.println("Collections initialized successfully.");
            populateRecommendedTable(); // Populate table after collections are initialized
        } else {
            System.err.println("Database is null. Cannot initialize collections.");
        }
    }

    public void setUserDetails(String userId, String sessionId) {
        this.currentUserId = userId;
        this.currentSessionId = sessionId;
    }

    // Populate the article table
    void populateRecommendedTable() {
        if (articlesCollection == null) {
            System.err.println("Articles collection not initialized.");
            return;
        }

        executorService.submit(() -> {
            try {
                // Fetch all articles from the collection
                List<Document> articles = articlesCollection.find().into(new ArrayList<>());
                System.out.println("Articles Retrieved: " + articles.size());

                if (articles.isEmpty()) {
                    // Display placeholder message if no articles are found
                    Platform.runLater(() -> recommendedTable.setPlaceholder(new Label("No articles available.")));
                    return;
                }

                // Convert MongoDB documents to Article objects
                List<Article> articleList = convertDocumentsToArticles(articles);

                // Update the table on the UI thread
                Platform.runLater(() -> recommendedTable.getItems().setAll(articleList));
            } catch (Exception e) {
                // Handle any errors during fetching or populating
                Platform.runLater(() -> showAlert("Error", "Failed to populate article table: " + e.getMessage(), Alert.AlertType.ERROR));
                e.printStackTrace();
            }
        });
    }

    // Convert database documents to Article objects
    private List<Article> convertDocumentsToArticles(List<Document> documents) {
        List<Article> articles = new ArrayList<>();
        for (Document doc : documents) {
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

    // View selected article
    @FXML
    public void viewArticle(ActionEvent actionEvent) {
        Article selectedArticle = recommendedTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert("Error", "No article selected.", Alert.AlertType.ERROR);
            return;
        }

        storeViewedArticle(selectedArticle);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            view_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserDetails(currentUserId, currentSessionId);
            controller.setArticleDetails(selectedArticle);

            currentStage.setScene(scene);
            currentStage.setTitle("View Article");
        } catch (IOException e) {
            showAlert("Error", "Failed to load article view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Store viewed article to user preferences
    private void storeViewedArticle(Article selectedArticle) {
        try {
            MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");
            Document userPreferencesDoc = userPreferencesCollection.find(new Document("user_id", currentUserId)).first();

            List<String> viewedArticles = userPreferencesDoc != null ?
                    userPreferencesDoc.getList("viewed_articles", String.class) : new ArrayList<>();

            if (!viewedArticles.contains(selectedArticle.getId())) {
                viewedArticles.add(selectedArticle.getId());
                userPreferencesCollection.updateOne(
                        new Document("user_id", currentUserId),
                        new Document("$set", new Document("viewed_articles", viewedArticles))
                );
            }
        } catch (Exception e) {
            Platform.runLater(() -> showAlert("Error", "Failed to store viewed article: " + e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    // Fetch and display recommended articles
    @FXML
    public void getRecommendation(ActionEvent actionEvent) {
        executorService.submit(() -> {
            try {
                MongoCollection<Document> userPreferencesCollection = database.getCollection("User_Preferences");
                Document userPreferencesDoc = userPreferencesCollection.find(new Document("user_id", currentUserId)).first();

                if (userPreferencesDoc == null) {
                    Platform.runLater(() -> showAlert("Info", "No preferences found for this user.", Alert.AlertType.INFORMATION));
                    return;
                }

                List<String> viewedArticles = userPreferencesDoc.getList("viewed_articles", String.class);
                List<String> preferredCategories = userPreferencesDoc.getList("preferred_categories", String.class);

                MongoCollection<Document> articlesCollection = database.getCollection("Articles");
                List<Document> recommendedDocs = articlesCollection.find(new Document("category", new Document("$in", preferredCategories))
                                .append("_id", new Document("$nin", viewedArticles)))
                        .into(new ArrayList<>());

                List<Article> recommendedArticles = convertDocumentsToArticles(recommendedDocs);

                Platform.runLater(() -> recommendedTable.getItems().setAll(recommendedArticles));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to fetch recommendations: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        });
    }

    // Navigate to main menu
    @FXML
    public void recommendedMainMenu(ActionEvent actionEvent) {
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

    // Exit the application
    @FXML
    public void recommendedExit(ActionEvent actionEvent) {
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

    // Display alert messages
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Shutdown the executor service
    public void shutdownExecutor() {
        executorService.shutdown();
    }

    @FXML
    private void initialize() {
        // Bind Article properties to TableView columns
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
                new SimpleStringProperty(cellData.getValue().getPublishedDate().toString()));

    }
}
