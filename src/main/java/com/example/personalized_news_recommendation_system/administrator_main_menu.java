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
        this.database = mongoClient.getDatabase("News_Recommendation");
        System.out.println("MongoClient set successfully in AdministratorMainMenu controller.");
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        this.database = database;
        System.out.println("Connected to database successfully.");
    }

    // Handle Add Articles Button Action
    @FXML
    public void addArticles(ActionEvent event) {
        try {
            // Load the Add Article page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_article.fxml"));
            Scene addArticleScene = new Scene(loader.load());

            // Get the controller of the Add Article page and pass MongoDB dependencies
            add_article addArticleController = loader.getController();
            addArticleController.setMongoClient(mongoClient);
            addArticleController.setDatabase(database);

            // Set the new scene to the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(addArticleScene);
            currentStage.setTitle("Add Article - Personalized News Recommendation System");
            currentStage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the Add Article page.");
            e.printStackTrace();
        }
    }

    // Handle Update Articles Button Action
    @FXML
    public void updateArticles(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manage_articles.fxml"));
            Scene updateArticleScene = new Scene(loader.load());

            manage_articles manageArticleController = loader.getController();
            manageArticleController.setMongoClient(mongoClient);
            manageArticleController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(updateArticleScene);
            currentStage.setTitle("Manage Articles - Personalized News Recommendation System");
            currentStage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the Manage Articles page.");
            e.printStackTrace();
        }
    }


    // Handle Exit Button Action
    @FXML
    public void Exit(ActionEvent event) {
        // Close the application
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    // Method to show a simple alert with a message
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
