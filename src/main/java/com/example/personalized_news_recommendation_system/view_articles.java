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

import java.time.LocalDate;

public class view_articles {

    // FXML Fields to bind with UI components
    @FXML
    public Label articleNameLabel;
    @FXML
    public Label articleCategoryLabel;
    @FXML
    public Label articleAuthorLabel;
    @FXML
    public Label articlePublishedDateLabel;
    @FXML
    public Label descriptionLabel;  // Corrected to match FXML field
    @FXML
    public Button readArticle;
    @FXML
    public Button skipArticle;
    @FXML
    public Button viewBack;
    @FXML
    public Button viewExit;
    @FXML
    public Button read;
    @FXML
    public Button skip;

    private Article article; // To store the article details
    private MongoClient mongoClient; // MongoClient to interact with the database
    private MongoDatabase database; // MongoDatabase to interact with specific collections

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    // This method is used to populate article details, including description and content
    public void setArticleDetails(Article article) {
        this.article = article;

        // Ensure article details are populated correctly in the UI
        articleNameLabel.setText("Article Name -: " + article.getName());
        articleCategoryLabel.setText("Category -: " + article.getCategory());
        articleAuthorLabel.setText("Author -: " + article.getAuthor());
        articlePublishedDateLabel.setText("Published Date -: " + formatDate(article.getPublishedDate()));
        descriptionLabel.setText("Description -: " + article.getDescription());
    }

    // Helper method to format date
    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "N/A";
    }

    // Read Article Button Clicked
    @FXML
    public void readArticle(ActionEvent actionEvent) {
        if (article != null && article.getContent() != null && !article.getContent().isEmpty()) {
            showAlert("Reading Article", "You are now reading the article: " + article.getContent());
            // You can implement reading functionality here, for example, show full article content in a new scene
        } else {
            showAlert("No Content", "Article content is unavailable.");
        }
    }

    // Skip Article Button Clicked
    @FXML
    public void skipArticle(ActionEvent actionEvent) {
        Stage currentStage = (Stage) skipArticle.getScene().getWindow();
        currentStage.close();
        openRecommendedArticlesPage();
    }

    // Open the Recommended Articles page after skipping an article
    private void openRecommendedArticlesPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recommended_articles.fxml"));
            Stage recommendedStage = new Stage();
            Scene recommendedScene = new Scene(loader.load());
            recommendedStage.setScene(recommendedScene);
            recommendedStage.setTitle("Recommended Articles");
            recommendedStage.show();

            // Set MongoClient and Database in the recommended articles controller
            recommended_articles recommendedArticlesController = loader.getController();
            recommendedArticlesController.setMongoClient(mongoClient);
            recommendedArticlesController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Recommended Articles page.");
        }
    }

    // View Back Button Clicked
    @FXML
    public void goBack(ActionEvent actionEvent) {
        Stage currentStage = (Stage) viewBack.getScene().getWindow();
        currentStage.close();
        openRecommendedArticlesPage();
    }

    // Exit the application
    @FXML
    public void exitApplication(ActionEvent actionEvent) {
        Stage stage = (Stage) viewExit.getScene().getWindow();
        stage.close(); // Closes the application window
    }

    // Show alert helper method
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
