package com.example.personalized_news_recommendation_system.Controller.UserController;

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
    private Button manageProfile;
    @FXML
    private Button recommendedArticles;
    @FXML
    private Button mainMenuExit;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String userId;
    private String sessionId;

    // Setters for MongoClient and MongoDatabase
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    // New setter for User ID and Session ID
    public void setUserInfo(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    @FXML
    public void manageProfile(ActionEvent event) {
        navigateToPage("/com/example/personalized_news_recommendation_system/manage_profile.fxml", "Manage Profile", event);
    }

    @FXML
    public void recommendedArticles(ActionEvent event) {
        navigateToPage("/com/example/personalized_news_recommendation_system/recommended_articles.fxml", "Recommended Articles", event);
    }

    @FXML
    public void mainMenuExit(ActionEvent event) {
        Platform.runLater(() -> {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        });
    }

    private void navigateToPage(String fxmlFileName, String pageTitle, ActionEvent event) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
                Scene targetScene = new Scene(loader.load());

                Object controller = loader.getController();

                // Pass userId and sessionId to target controllers
                if (controller instanceof recommended_articles) {
                    recommended_articles recommendedController = (recommended_articles) controller;
                    recommendedController.setMongoClient(mongoClient);
                    recommendedController.setDatabase(mongoClient.getDatabase("News_Recommendation"));
                    recommendedController.setUserDetails(userId, sessionId);
                } else if (controller instanceof manage_profile) {
                    manage_profile manageProfile = (manage_profile) controller;
                    manageProfile.setMongoClient(mongoClient);
                    manageProfile.setDatabase(mongoClient.getDatabase("News_Recommendation"));
                    manageProfile.setUserInfo(userId, sessionId);
                }

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(targetScene);
                currentStage.setTitle(pageTitle + " - Personalized News Recommendation System");
                currentStage.show();
            } catch (IOException e) {
                showAlert("Navigation Error", "Failed to load the " + pageTitle + " page.");
                e.printStackTrace();
            } catch (Exception ex) {
                showAlert("Controller Error", "Failed to initialize the controller for " + pageTitle + ".");
                ex.printStackTrace();
            }
        });
    }

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
