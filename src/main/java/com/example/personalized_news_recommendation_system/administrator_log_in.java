package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

public class administrator_log_in {

    @FXML
    public Button administratorSignIn;
    @FXML
    public Button adminLogInHome;
    @FXML
    private TextField administratorUsername;
    @FXML
    private PasswordField administratorPassword;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> adminCollection;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in AdministratorLogIn controller.");
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.adminCollection = database.getCollection("Admin");
            System.out.println("Connected to Admin collection with " + adminCollection.countDocuments() + " documents.");
        } else {
            System.out.println("Database is null in AdministratorLogIn controller.");
        }
    }

    // Sign-In Method for Administrator
    @FXML
    void administratorSignIn(ActionEvent event) {
        String username = administratorUsername.getText();
        String password = administratorPassword.getText();

        // Check if the username or password fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }

        // Authenticate admin synchronously in the UI thread
        boolean authenticated = authenticateAdmin(username, password);
        if (authenticated) {
            showAlert("Sign-In Success", "Welcome, Administrator!", Alert.AlertType.INFORMATION);
            openAdminMainMenu(event);
        } else {
            showAlert("Sign-In Failed", "Invalid credentials.", Alert.AlertType.ERROR);
            administratorPassword.clear();
        }
    }

    // Method to authenticate admin
    private Boolean authenticateAdmin(String username, String password) {
        Document usernameQuery = new Document("username", username);
        Document adminDoc = adminCollection.find(usernameQuery).first();
        if (adminDoc != null) {
            String storedPassword = adminDoc.getString("password");
            return storedPassword != null && storedPassword.equals(password);
        }
        return false;
    }

    // Method to show a simple alert with a message
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Navigate to Admin main menu
    public void openAdminMainMenu(ActionEvent actionEvent) {
        try {
            // Load the Admin Main Menu page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_main_menu.fxml"));
            Scene adminMainMenuScene = new Scene(loader.load());

            // Get the controller for the Admin Main Menu and pass the MongoClient and MongoDatabase
            administrator_main_menu adminMainMenuController = loader.getController();
            adminMainMenuController.setMongoClient(mongoClient);
            adminMainMenuController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(adminMainMenuScene);
            currentStage.setTitle("Personalized News Recommendation System - Administrator Main Menu");
            currentStage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the Admin Main Menu page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred while navigating.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void adminLogInHome(ActionEvent actionEvent) {
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
