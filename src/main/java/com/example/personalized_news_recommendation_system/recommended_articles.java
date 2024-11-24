package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class recommended_articles {

    @FXML
    private Button recommendedMainMenu;
    @FXML
    private Button recommendedExit;
    @FXML
    private Button viewArticle;
    @FXML
    private Button getRecommendation;
    @FXML
    private TableView<Article> recommendedTable; // Updated to Article class
    @FXML
    private TableColumn<Article, String> articleNameColumn;
    @FXML
    private TableColumn<Article, String> categoryColumn;
    @FXML
    private TableColumn<Article, String> authorColumn;
    @FXML
    private TableColumn<Article, String> publishedDateColumn;

    private MongoClient mongoClient;
    private MongoCollection<Document> articlesCollection;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            System.out.println("Articles collection initialized successfully.");
            populateArticleTableSafely(); // Populate the table after initialization
        } else {
            System.out.println("Database is null. Articles collection not initialized.");
        }
    }

    // Method to populate the articles table
    private void populateArticleTable() {
        executorService.submit(() -> {
            try {
                if (articlesCollection == null) {
                    Platform.runLater(() -> showAlert("Error", "Articles collection is not initialized."));
                    return;
                }

                List<Document> documents = articlesCollection.find().into(new ArrayList<>());
                if (documents.isEmpty()) {
                    Platform.runLater(() -> showAlert("Information", "No recommended articles found."));
                    return;
                }

                List<Article> articles = new ArrayList<>();
                for (Document doc : documents) {
                    String name = doc.getString("article_name");
                    String category = doc.getString("category");
                    String author = doc.getString("author");
                    String publishedDate = doc.getString("published_date");
                    String description = doc.getString("description");
                    String content = doc.getString("content");

                    // Create an Article object and add to the list
                    articles.add(new Article(name, category, author, publishedDate, description, content));
                }

                // Configure table columns
                articleNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
                categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
                authorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthor()));
                publishedDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPublishedDate().toString()));

                // Populate the table
                Platform.runLater(() -> recommendedTable.getItems().setAll(articles));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to populate articles table: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    // Safely populate the article table after ensuring initialization
    private void populateArticleTableSafely() {
        if (articlesCollection != null) {
            populateArticleTable();
        } else {
            System.out.println("Articles collection is not yet initialized.");
        }
    }

    // Button action to fetch recommendations
    @FXML
    public void getRecommendation(ActionEvent actionEvent) {
        populateArticleTableSafely();
    }

    // Navigate to the main menu
    @FXML
    public void recommendedMainMenu(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            try {
                // Close current stage
                Stage stage = (Stage) recommendedMainMenu.getScene().getWindow();
                stage.close();

                // Load the Main Menu scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("user_main_menu.fxml"));
                Stage mainMenuStage = new Stage();
                mainMenuStage.setScene(new Scene(loader.load()));
                mainMenuStage.setTitle("Main Menu");
                mainMenuStage.show();

                // Pass MongoClient and set database when returning
                mainMenuStage.setOnCloseRequest(e -> {
                    // Ensure database is reinitialized when coming back to recommended_articles
                    if (mongoClient != null) {
                        setDatabase(mongoClient.getDatabase("News_Recommendation")); // Repass the MongoDatabase
                        populateArticleTableSafely(); // Refresh the table
                    } else {
                        System.out.println("MongoClient is null when returning from main menu.");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to open the Main Menu: " + e.getMessage());
            }
        });
    }

    // Exit the application
    @FXML
    public void recommendedExit(ActionEvent actionEvent) {
        executorService.submit(() -> {
            Stage stage = (Stage) recommendedExit.getScene().getWindow();
            stage.close();
        });
    }

    // Show alert helper method
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Initialize method to set up the UI when the scene is loaded
    @FXML
    public void initialize() {
        System.out.println("Controller initialized.");
        // Attempt to populate the table; this will only succeed if articlesCollection is already initialized
        populateArticleTableSafely();
    }

    @FXML
    public void viewArticle(ActionEvent actionEvent) {
        // Get the selected article from the table
        Article selectedArticle = recommendedTable.getSelectionModel().getSelectedItem();

        if (selectedArticle == null) {
            // If no article is selected, show an alert
            showAlert("Error", "No article selected.");
            return;
        }

        // Pass the article to the view_article controller
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("view_articles.fxml"));
                Stage viewArticleStage = new Stage();
                Scene scene = new Scene(loader.load());

                // Get the controller of the new scene
                view_articles viewArticleController = loader.getController();

                // Pass the article object to the view_article controller
                viewArticleController.setArticleDetails(selectedArticle);

                // Show the new scene on the FX application thread
                viewArticleStage.setScene(scene);
                viewArticleStage.setTitle("View Article");
                viewArticleStage.show();

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Failed to open the article view: " + e.getMessage()));
            }
        });
    }
}
