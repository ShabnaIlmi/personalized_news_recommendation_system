package com.example.personalized_news_recommendation_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class view_articles {

    @FXML
    private Text articleName;
    @FXML
    private Text articleAuthorValue;
    @FXML
    private Text articleCategoryValue;
    @FXML
    private Text articlePublishedDateValue;
    @FXML
    private Text descriptionValue;

    @FXML
    private Button viewBack;
    @FXML
    private Button read;
    @FXML
    private Button skip;
    @FXML
    private Button viewExit;

    private Article article;
    private MongoClient mongoClient;
    private MongoDatabase database;

    // Inject MongoClient for database interactions
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Inject MongoDatabase for operations
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    // Set the article details to be displayed
    public void setArticleDetails(Article article) {
        this.article = article;

        if (article != null) {
            articleName.setText(article.getName() != null ? article.getName() : "N/A");
            articleAuthorValue.setText(article.getAuthor() != null ? article.getAuthor() : "N/A");
            articleCategoryValue.setText(article.getCategory() != null ? article.getCategory() : "N/A");
            articlePublishedDateValue.setText(
                    article.getPublishedDate() != null ? article.getPublishedDate().toString() : "N/A"
            );
            descriptionValue.setText(article.getDescription() != null ? article.getDescription() : "N/A");
        } else {
            showAlert("No Article Found", "Article details are unavailable.");
        }
    }

    @FXML
    private void readArticle(ActionEvent actionEvent) {
        if (article == null || article.getContent() == null || article.getContent().isEmpty()) {
            showAlert("No Content", "Article content is unavailable.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("read_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) read.getScene().getWindow();

            // Passing data to the next controller
            read_articles controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setArticleDetails(article);

            currentStage.setScene(scene);
            currentStage.setTitle("Read Article");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Read Article page: " + e.getMessage());
        }
    }

    @FXML
    private void skipArticle(ActionEvent actionEvent) {
        navigateToRecommendedArticles();
    }

    @FXML
    private void goBack(ActionEvent actionEvent) {
        navigateToRecommendedArticles();
    }

    @FXML
    private void exitApplication(ActionEvent actionEvent) {
        Stage stage = (Stage) viewExit.getScene().getWindow();
        stage.close();
    }

    private void navigateToRecommendedArticles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recommended_articles.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) viewBack.getScene().getWindow();

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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
