package com.example.personalized_news_recommendation_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class view_articles {

    @FXML
    public Label articleNameLabel;
    @FXML
    public Label articleCategoryLabel;
    @FXML
    public Label articleAuthorLabel;
    @FXML
    public Label articlePublishedDateLabel;
    @FXML
    public Label articleDescriptionLabel; // New label for description
    @FXML
    public Button readArticle;
    @FXML
    public Button skipArticle;

    private Article article;

    // Set article details, including the description
    public void setArticleDetails(String articleName, String articleContent, String category, String author, String publishedDate) {
        articleNameLabel.setText("Article Name -: " + articleName);
        articleCategoryLabel.setText("Category -: " + category);
        articleAuthorLabel.setText("Author -: " + author);
        articlePublishedDateLabel.setText("Published Date -: " + publishedDate);
        articleDescriptionLabel.setText("Description -: " + articleContent);  // Set the description text here
    }

    // Read Article Button Clicked
    @FXML
    public void readArticle(ActionEvent actionEvent) {
        if (article != null) {
            showAlert("Reading Article", "You are now reading: " + article.getName());
            // Implement reading functionality here, e.g., open another scene with article content
        }
    }

    // Skip Article Button Clicked
    @FXML
    public void skipArticle(ActionEvent actionEvent) {
        // Close the current view (view_article page)
        Stage currentStage = (Stage) skipArticle.getScene().getWindow();
        currentStage.close();

        // Open the recommended_articles page
        openRecommendedArticlesPage();
    }

    private void openRecommendedArticlesPage() {
        try {
            // Load the recommended articles FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recommended_articles.fxml"));
            Stage recommendedStage = new Stage();
            Scene recommendedScene = new Scene(loader.load());
            recommendedStage.setScene(recommendedScene);
            recommendedStage.setTitle("Recommended Articles");
            recommendedStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Recommended Articles page.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
