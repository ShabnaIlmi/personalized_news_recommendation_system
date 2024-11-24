package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class user_main_menu {

    @FXML
    private Button manageArticles;
    @FXML
    private Button recommendedArticles;
    @FXML
    private Button readArticles;
    @FXML
    private Button viewArticles;
    @FXML
    private Button mainMenuExit;

    private MongoClient mongoClient;
    private MongoDatabase database;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in UserMainMenu controller.");
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        this.database = database;
        System.out.println("Connected to database successfully.");
    }

    // Handle Manage Articles Button Action
    @FXML
    public void manageArticles(ActionEvent event) {
        navigateToPage("manage_articles.fxml", "Manage Articles", event);
    }

    // Handle Recommended Articles Button Action
    @FXML
    public void recommendedArticles(ActionEvent event) {
        navigateToPage("recommended_articles.fxml", "Recommended Articles", event);
    }

    // Handle Read Articles Button Action
    @FXML
    public void readArticles(ActionEvent event) {
        navigateToPage("read_articles.fxml", "Read Articles", event);
    }

    // Handle View Articles Button Action
    @FXML
    public void viewArticles(ActionEvent event) {
        navigateToPage("view_articles.fxml", "View Articles", event);
    }

    // Handle Exit Button Action
    @FXML
    public void mainMenuExit(ActionEvent event) {
        Platform.runLater(() -> {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        });
    }

    // Method to navigate to a specific page
    private void navigateToPage(String fxmlFileName, String pageTitle, ActionEvent event) {
        Platform.runLater(() -> {
            try {
                // Load the target FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
                Scene targetScene = new Scene(loader.load());

                // Get the controller of the target page and pass MongoDB dependencies
                Object controller = loader.getController();

                if (controller instanceof recommended_articles) {
                    recommended_articles recommendedController = (recommended_articles) controller;
                    recommendedController.setMongoClient(mongoClient);
                    recommendedController.setDatabase(database);
                    recommendedController.initialize();  // Ensure table is populated
                } else if (controller instanceof manage_profile) {
                    manage_profile manageController = (manage_profile) controller;
                    // manageController.setMongoClient(mongoClient);  // Pass MongoDB if needed
                    // manageController.setDatabase(database);     // Pass MongoDB if needed
                } else if (controller instanceof read_articles) {
                    read_articles readController = (read_articles) controller;
                    // readController.setMongoClient(mongoClient);
                    // readController.setDatabase(database);
                } else if (controller instanceof view_articles) {
                    view_articles viewController = (view_articles) controller;
                    viewController.setMongoClient(mongoClient);
                    viewController.setDatabase(database);
                }

                // Set the new scene to the current stage
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(targetScene);
                currentStage.setTitle(pageTitle + " - Personalized News Recommendation System");
                currentStage.show();
            } catch (IOException e) {
                showAlert("Navigation Error", "Failed to load the " + pageTitle + " page.");
                e.printStackTrace();
            }
        });
    }

    // Method to show a simple alert with a message
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
