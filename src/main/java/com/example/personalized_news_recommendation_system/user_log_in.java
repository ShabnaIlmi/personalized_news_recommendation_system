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

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.userCollection = database.getCollection("User");
        }
    }

    @FXML
    void signIn(ActionEvent event) {
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

        try {
            Document userDoc = userCollection.find(new Document("username", username)).first();

            if (userDoc != null) {
                String storedPassword = userDoc.getString("password");

                if (storedPassword != null && storedPassword.equals(password)) {
                    String firstName = userDoc.getString("first_name");
                    String lastName = userDoc.getString("last_name");

                    showAlert("Sign-In Success", "Welcome, " + firstName + " " + lastName + "!", Alert.AlertType.INFORMATION);
                    navigateToMainMenu(event);
                } else {
                    showAlert("Sign-In Failed", "Invalid password.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Sign-In Failed", "User not found.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void navigateToMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_main_menu.fxml"));
            Scene userMainMenuScene = new Scene(loader.load());

            user_main_menu userMainMenuController = loader.getController();
            userMainMenuController.setMongoClient(mongoClient);
            userMainMenuController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(userMainMenuScene);
            currentStage.setTitle("Personalized News Recommendation System - Main Menu");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the main menu.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void userSignHome(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("homePage.fxml"));
            Scene homeScene = new Scene(loader.load());

            homePage homeController = loader.getController();
            homeController.setMongoClient(mongoClient);
            homeController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(homeScene);
            currentStage.setTitle("Personalized News Recommendation System - Home");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the home page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}
