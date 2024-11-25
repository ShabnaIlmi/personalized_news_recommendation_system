package com.example.personalized_news_recommendation_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class read_articles {

    @FXML
    public Label articleNameLabel;
    @FXML
    public Label articlePublishedDateLabel;
    @FXML
    public Label articleAuthorLabel;
    @FXML
    public Label articleContentLabel;
    @FXML
    public Button likeButton;
    @FXML
    public Button dislikeButton;
    @FXML
    public Button backButton;
    @FXML
    public Button exitButton;

    private Article article;
    private MongoClient mongoClient;
    private MongoDatabase database;

    // Setters for MongoClient and MongoDatabase
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    // Set article details to display
    public void setArticleDetails(Article article) {
        this.article = article;

        articleNameLabel.setText(article.getName());
        articlePublishedDateLabel.setText("Published: " +
                (article.getPublishedDate() != null
                        ? article.getPublishedDate()
                        : "N/A")
        );
        articleAuthorLabel.setText("Author: " + article.getAuthor());
        articleContentLabel.setText(article.getContent());
    }

    // Handle "Like" button action
    @FXML
    public void handleLike(ActionEvent actionEvent) {
        if (article != null) {
            // Logic for liking an article (e.g., update a "likes" field in the database)
            showAlert("Liked", "You liked this article!");
        } else {
            showAlert("Error", "No article loaded.");
        }
    }

    // Handle "Dislike" button action
    @FXML
    public void handleDislike(ActionEvent actionEvent) {
        if (article != null) {
            // Logic for disliking an article (e.g., update a "dislikes" field in the database)
            showAlert("Disliked", "You disliked this article.");
        } else {
            showAlert("Error", "No article loaded.");
        }
    }

    // Navigate back to the "View Article" page
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recommended_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) backButton.getScene().getWindow();

            // Passing data to the next controller
            recommended_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.populateArticleTable();

            currentStage.setScene(scene);
            currentStage.setTitle("Recommended Articles");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Recommended Articles page: " + e.getMessage());
        }
    }

    // Handle "Exit" button action
    @FXML
    public void handleExit(ActionEvent actionEvent) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close(); // Close the application
    }

    // Helper method to show alerts
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
