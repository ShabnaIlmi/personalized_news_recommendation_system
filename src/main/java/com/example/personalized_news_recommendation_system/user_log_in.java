package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class user_log_in {

    @FXML
    private Button signIn;

    @FXML
    private Button userSignHome;

    @FXML
    private TextField userSignUsername;

    @FXML
    private PasswordField userSignPassword;

    private MongoClient mongoClient;
    private MongoCollection<Document> userCollection;
    private ExecutorService executorService; // For background tasks

    // Method to initialize ExecutorService
    private void initializeExecutorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }
    }

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.userCollection = database.getCollection("User");
        }
    }

    // Setter for ExecutorService
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @FXML
    void signIn(ActionEvent event) {
        // Initialize ExecutorService if it's null
        if (executorService == null) {
            initializeExecutorService();
        }

        String username = userSignUsername.getText();
        String password = userSignPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }

        if (mongoClient == null || userCollection == null) {
            showAlert("Database Error", "Database connection is not initialized.", Alert.AlertType.ERROR);
            return;
        }

        // Perform the database query in a background thread
        executorService.submit(() -> {
            try {
                Document userDoc = userCollection.find(new Document("username", username)).first();

                if (userDoc != null) {
                    String storedPassword = userDoc.getString("password");

                    if (storedPassword != null && storedPassword.equals(password)) {
                        String firstName = userDoc.getString("first_name");
                        String lastName = userDoc.getString("last_name");

                        Platform.runLater(() -> {
                            showAlert("Sign-In Success", "Welcome, " + firstName + " " + lastName + "!", Alert.AlertType.INFORMATION);
                            navigateToMainMenu(event);
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Sign-In Failed", "Invalid password.", Alert.AlertType.ERROR));
                    }
                } else {
                    Platform.runLater(() -> showAlert("Sign-In Failed", "User not found.", Alert.AlertType.ERROR));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR));
                e.printStackTrace();
            }
        });
    }

    private void navigateToMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_main_menu.fxml"));
            Scene userMainMenuScene = new Scene(loader.load());

            // Pass the MongoClient and MongoDatabase to the controller
            user_main_menu userMainMenuController = loader.getController();
            userMainMenuController.setMongoClient(mongoClient);
            userMainMenuController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(userMainMenuScene);
            currentStage.setTitle("Personalized News Recommendation System - User Main Menu");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the main menu.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void userSignHome(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("homePage.fxml"));
            Scene homeScene = new Scene(loader.load());

            // Pass the MongoClient and MongoDatabase to the controller
            homePage homeController = loader.getController();
            homeController.setMongoClient(mongoClient);
            homeController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(homeScene);
            currentStage.setTitle("Personalized News Recommendation System - Home");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the home page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Method to show alert messages
    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Graceful shutdown of ExecutorService
    public void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // Register a shutdown hook for graceful shutdown of ExecutorService
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownExecutorService));
    }
}
