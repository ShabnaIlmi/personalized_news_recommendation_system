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
    private TableView<Article> recommendedTable;
    @FXML
    private TableColumn<Article, String> articleNameColumn;
    @FXML
    private TableColumn<Article, String> categoryColumn;
    @FXML
    private TableColumn<Article, String> authorColumn;
    @FXML
    private TableColumn<Article, String> publishedDateColumn;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> articlesCollection;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
        reinitializeArticlesCollection();
    }

    private void reinitializeArticlesCollection() {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
        }
    }

    public void populateArticleTable() {
        executorService.submit(() -> {
            try {
                if (articlesCollection == null) {
                    //Platform.runLater(() -> showAlert("Error", "Articles collection is not initialized."));
                    return;
                }

                List<Document> documents = articlesCollection.find().into(new ArrayList<>());
                List<Article> articles = new ArrayList<>();
                for (Document doc : documents) {
                    String name = doc.getString("article_name");
                    String category = doc.getString("category");
                    String author = doc.getString("author");
                    String publishedDate = doc.getString("published_date");
                    String description = doc.getString("description");
                    String content = doc.getString("content");

                    articles.add(new Article(name, category, author, publishedDate, description, content));
                }

                Platform.runLater(() -> {
                    articleNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
                    categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
                    authorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthor()));
                    publishedDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                            cellData.getValue().getPublishedDate() != null
                                    ? cellData.getValue().getPublishedDate().toString()
                                    : "N/A"
                    ));
                    recommendedTable.getItems().setAll(articles);
                });

            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to populate articles table: " + e.getMessage()));
            }
        });
    }

    @FXML
    public void initialize() {
        populateArticleTable();
    }

    @FXML
    public void getRecommendation(ActionEvent actionEvent) {
        populateArticleTable();
    }

    @FXML
    public void viewArticle(ActionEvent actionEvent) {
        Article selectedArticle = recommendedTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert("Error", "No article selected.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) recommendedTable.getScene().getWindow();

            view_articles viewController = loader.getController();
            viewController.setMongoClient(mongoClient);
            viewController.setDatabase(database);
            viewController.setArticleDetails(selectedArticle);

            currentStage.setScene(scene);
            currentStage.setTitle("View Article");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the article view: " + e.getMessage());
        }
    }

    @FXML
    public void recommendedMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_main_menu.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) recommendedMainMenu.getScene().getWindow();

            user_main_menu mainMenuController = loader.getController();
            mainMenuController.setMongoClient(mongoClient);
            mainMenuController.setDatabase(database);

            currentStage.setScene(scene);
            currentStage.setTitle("Main Menu");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the Main Menu.");
        }
    }

    @FXML
    public void recommendedExit(ActionEvent actionEvent) {
        Stage stage = (Stage) recommendedExit.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
