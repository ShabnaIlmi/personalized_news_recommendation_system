package com.example.personalized_news_recommendation_system.User;

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

public class manage_profile {
    @FXML
    public Button accountInformation;
    @FXML
    public Button manageExit;
    @FXML
    public Button deleteMyAccount;
    @FXML
    public Button manageMainMenu;
    @FXML
    public Button signOut;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String userId;
    private String sessionId;

    // Setters for MongoClient and MongoDatabase
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in Manage Profile controller.");
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
        System.out.println("Connected to database successfully.");
    }

    // New setter for User ID and Session ID
    public void setUserInfo(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    @FXML
    public void accountInformation(ActionEvent event) {
        navigateToPage("/com/example/personalized_news_recommendation_system/account_information.fxml", "Account Information", event);
    }

    @FXML
    public void deleteMyAccount(ActionEvent event) {
        navigateToPage("/com/example/personalized_news_recommendation_system/delete_account.fxml", "Delete Account", event);
    }

    @FXML
    public void manageMainMenu(ActionEvent actionEvent) {
        navigateToPage("user_main_menu.fxml", "/com/example/personalized_news_recommendation_system/User Main Menu", actionEvent);
    }

    @FXML
    public void manageExit(ActionEvent actionEvent) {
        // Close the application
        Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

    @FXML
    public void signOut(ActionEvent actionEvent) {
        // Show an alert confirming the user is signing out
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sign Out");
        alert.setHeaderText(null);
        alert.setContentText("You are signing out of your account.");
        alert.showAndWait();

        // Record the sign-out action in the User_Log collection
        if (database != null) {
            try {
                var userLogCollection = database.getCollection("User_Log");
                var currentDateTime = java.time.LocalDateTime.now().toString();

                var logEntry = new org.bson.Document("user_id", userId)
                        .append("session_id", sessionId)
                        .append("action", "Sign-Out")
                        .append("logged_date_time", currentDateTime);

                userLogCollection.insertOne(logEntry);
                System.out.println("Sign-out action recorded in User_Log collection.");
            } catch (Exception e) {
                showAlert("Database Error", "Failed to record the sign-out action in the database.");
                e.printStackTrace();
            }
        } else {
            showAlert("Database Connection Error", "Database connection is not established. Action not logged.");
        }

        // Close the application
        Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }


    private void navigateToPage(String fxmlFileName, String pageTitle, ActionEvent event) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
                Scene targetScene = new Scene(loader.load());

                // Get the controller of the target page
                Object controller = loader.getController();

                // Pass MongoClient, MongoDatabase, userId, and sessionId to the target controller
                if (controller instanceof delete_account) {
                    delete_account deleteAccountController = (delete_account) controller;
                    deleteAccountController.setMongoClient(mongoClient);
                    deleteAccountController.setDatabase(mongoClient.getDatabase("News_Recommendation"));
                    deleteAccountController.setUserInfo(userId, sessionId);
                } else if (controller instanceof account_information) {
                    account_information accountInfoController = (account_information) controller;
                    accountInfoController.setMongoClient(mongoClient);
                    accountInfoController.setDatabase(mongoClient.getDatabase("News_Recommendation"));
                    accountInfoController.setUserInfo(userId, sessionId);
                } else if (controller instanceof user_main_menu) {
                    user_main_menu userMainMenuController = (user_main_menu) controller;
                    userMainMenuController.setMongoClient(mongoClient);
                    userMainMenuController.setDatabase(mongoClient.getDatabase("News_Recommendation"));
                    userMainMenuController.setUserInfo(userId, sessionId);
                }

                // Set the new scene
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(targetScene);
                currentStage.setTitle(pageTitle);
                currentStage.show();
            } catch (IOException e) {
                showAlert("Navigation Error", "Failed to load the " + pageTitle + " page: " + e.getMessage());
                e.printStackTrace();
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
