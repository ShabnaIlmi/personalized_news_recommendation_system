package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class adminstrator_main_menu {

    @FXML
    public Button addArticles;
    @FXML
    public Button updateArticles;
    @FXML
    public Button Exit;

    private MongoDatabase database;

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    @FXML
    public void addArticles(ActionEvent actionEvent) {
        try {
            // Load the Add Article page (FXML file for adding articles)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_article.fxml"));
            Scene addArticleScene = new Scene(loader.load());

            // Get the AddArticle controller and set the MongoDB dependencies
            add_article addArticleController = loader.getController();
            addArticleController.setDatabase(database);

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(addArticleScene);
            currentStage.setTitle("Add Article - Personalized News Recommendation System");
            currentStage.show();

        } catch (IOException e) {
            showError("Navigation Error", "Failed to load the Add Article page.");
            e.printStackTrace();
        }
    }

    @FXML
    public void updateArticles(ActionEvent actionEvent) {
        try {
            // Load the Update Article page (FXML file for updating articles)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("update_article.fxml"));
            Scene updateArticleScene = new Scene(loader.load());

            // Get the UpdateArticle controller and set the MongoDB dependencies
            update_article updateArticleController = loader.getController();
            updateArticleController.setDatabase(database);

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(updateArticleScene);
            currentStage.setTitle("Update Article - Personalized News Recommendation System");
            currentStage.show();

        } catch (IOException e) {
            showError("Navigation Error", "Failed to load the Update Article page.");
            e.printStackTrace();
        }
    }

    @FXML
    public void Exit(ActionEvent actionEvent) {
        // Close the application or navigate back to the home page
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

    // Method to show an error alert
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMongoClient(MongoClient mongoClient) {
    }

}
