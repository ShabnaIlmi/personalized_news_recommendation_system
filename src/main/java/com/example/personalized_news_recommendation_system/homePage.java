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

public class homePage {

    @FXML
    public Button homeSignUp;
    @FXML
    public Button homeLogIn;
    @FXML
    public Button administerLogIn;

    private MongoClient mongoClient;
    private MongoDatabase database;

    // Setter for MongoClient and MongoDatabase
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully.");
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
        System.out.println("Connected to database: " + database.getName());
    }

    // Method to handle the Log In button click and navigate to user_log_in.fxml
    @FXML
    public void homeLogIn(ActionEvent actionEvent) {
        try {
            // Load the sign_in page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_log_in.fxml"));
            Scene signInScene = new Scene(loader.load());

            // Get the sign_in controller and set the MongoDB dependencies
            user_log_in signInController = loader.getController();
            signInController.setMongoClient(mongoClient);
            signInController.setDatabase(database);

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(signInScene);
            currentStage.setTitle("Personalized News Recommendation System - Sign In");
            currentStage.show();

        } catch (IOException e) {
            showError("Navigation Error", "Failed to load the user log-in page.");
            e.printStackTrace();
        }
    }

    // Method to handle the Sign Up button click and navigate to user_sign_up.fxml
    @FXML
    public void homeSignUp(ActionEvent actionEvent) {
        try {
            // Load the sign-up page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("_user_sign_up.fxml"));
            Scene signUpScene = new Scene(loader.load());

            // Get the sign_in controller and set the MongoDB dependencies
            user_sign_up signUpController = loader.getController();
            signUpController.setMongoClient(mongoClient);
            signUpController.setDatabase(database);

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(signUpScene);
            currentStage.setTitle("Personalized News Recommendation System - Sign Up");
            currentStage.show();

        } catch (IOException e) {
            showError("Navigation Error", "Failed to load the sign-up page.");
            e.printStackTrace();
        }
    }

    @FXML
    public void administerLogIn(ActionEvent actionEvent) {
        try {
            // Load the admin login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_log_in.fxml"));
            Scene adminLoginScene = new Scene(loader.load());

            // Get the sign_in controller and set the MongoDB dependencies
            administrator_log_in administratorLogInController = loader.getController();
            administratorLogInController.setMongoClient(mongoClient);
            administratorLogInController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(adminLoginScene);
            currentStage.setTitle("Personalized News Recommendation System - Admin Login");
            currentStage.show();

        } catch (IOException e) {
            showError("Navigation Error", "Failed to load the admin login page.");
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
