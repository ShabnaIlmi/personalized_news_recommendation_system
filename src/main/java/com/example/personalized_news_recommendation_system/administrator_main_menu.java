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

public class administrator_main_menu {

    @FXML
    private Button addArticles;
    @FXML
    private Button updateArticles;
    @FXML
    private Button Exit;

    private MongoClient mongoClient;
    private MongoDatabase database;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in administrator_main_menu.");
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        this.database = database;
        if (database != null) {
            System.out.println("Connected to database: " + database.getName());
        } else {
            System.out.println("Database is not set in administrator_main_menu.");
        }
    }

    @FXML
    public void addArticles(ActionEvent actionEvent) {
        navigateToAddArticlePage(actionEvent);
    }

    @FXML
    public void updateArticles(ActionEvent actionEvent) {
        navigateToUpdateArticlePage(actionEvent);
    }

    private void navigateToAddArticlePage(ActionEvent actionEvent) {
        try {
            // Load the Add Article page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_article.fxml"));
            Scene addArticleScene = new Scene(loader.load());

            // Get the Add Article controller and set the MongoDB dependencies
            add_article addArticleController = loader.getController();
            addArticleController.setMongoClient(mongoClient);
            addArticleController.setDatabase(database);

            // Debug log
            System.out.println("Navigating to Add Article page with database: " + database.getName());

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(addArticleScene);
            currentStage.setTitle("Personalized News Recommendation System - Add Article");
            currentStage.show();

        } catch (IOException e) {
            showError("Navigation Error", "Failed to load the Add Article page.");
            e.printStackTrace();
        }
    }

    private void navigateToUpdateArticlePage(ActionEvent actionEvent) {
        try {
            // Load the Update Article page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("update_article.fxml"));
            Scene updateArticleScene = new Scene(loader.load());

            // Get the Update Article controller and set the MongoDB dependencies
            update_articles updateArticleController = loader.getController();
            updateArticleController.setMongoClient(mongoClient);
            updateArticleController.setDatabase(database);

            // Debug log
            System.out.println("Navigating to Update Article page with database: " + database.getName());

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(updateArticleScene);
            currentStage.setTitle("Personalized News Recommendation System - Update Article");
            currentStage.show();

        } catch (IOException e) {
            showError("Navigation Error", "Failed to load the Update Article page.");
            e.printStackTrace();
        }
    }

    @FXML
    public void Exit(ActionEvent actionEvent) {
        // Close the application
        System.out.println("Exiting the application.");
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
}
