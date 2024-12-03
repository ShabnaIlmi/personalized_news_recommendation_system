package com.example.personalized_news_recommendation_system.Controller.UserController;

import com.example.personalized_news_recommendation_system.Driver.homePage;
import com.example.personalized_news_recommendation_system.Utils.Validator;
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
import java.time.LocalDateTime;

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
    private MongoCollection<Document> userLogCollection;
    private String currentUserId;  // Store the user ID for the session
    private String currentSessionId;

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.userCollection = database.getCollection("User");
            this.userLogCollection = database.getCollection("User_Logs");
        }
    }

    @FXML
    void signIn(ActionEvent event) {
        String username = userSignUsername.getText();
        String password = userSignPassword.getText();

        // Validate input fields
        if (!Validator.areFieldsNotEmpty(username, password)) {
            showAlert("Input Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }

        // Validate database collections
        if (!Validator.areCollectionsSet(userCollection, userLogCollection)) {
            showAlert("Database Error", "Database is not properly configured.", Alert.AlertType.ERROR);
            return;
        }

        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc == null) {
            showAlert("Database Error", "User record not found.", Alert.AlertType.ERROR);
            return;
        }

        // Authenticate user
        if (Validator.authenticateUser(userCollection, username, password)) {
            logUserLogin(username);  // Log the user login event

            String firstName = userDoc.getString("first_name");
            String lastName = userDoc.getString("last_name");

            logUserLogin(username);  // Log the user login event

            // Show success message with first and last name
            showAlert("Login Successful", "Welcome, " + firstName + " " + lastName + "! You have logged in successfully.", Alert.AlertType.INFORMATION);


            navigateToMainMenu(event);  // Navigate to main menu after successful login
        } else {
            showAlert("Log-In Error", "Incorrect username or password.", Alert.AlertType.ERROR);
        }
    }

    private void logUserLogin(String username) {
        String sessionId = generateSessionId(username);  // Generate session ID for log-in

        // Log the login action with user ID and session ID
        Document log = new Document("user_id", username)
                .append("session_id", sessionId)
                .append("action", "Log-In")
                .append("logged_date_time", LocalDateTime.now().toString());
        userLogCollection.insertOne(log);

        // Set the current user and session IDs for future reference
        currentUserId = username;
        currentSessionId = sessionId;
    }

    private String generateSessionId(String username) {
        Document lastSessionDoc = userLogCollection.find(new Document("user_id", username))
                .sort(new Document("logged_date_time", -1)).first();

        int lastSessionNumber = 0;
        if (lastSessionDoc != null) {
            String lastSessionId = lastSessionDoc.getString("session_id");
            lastSessionNumber = Integer.parseInt(lastSessionId);
        }

        return String.format("%03d", lastSessionNumber + 1);  // Increment session number
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateToMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/user_main_menu.fxml"));
            Scene userMainMenuScene = new Scene(loader.load());

            user_main_menu userMainMenuController = loader.getController();
            userMainMenuController.setMongoClient(mongoClient);
            userMainMenuController.setDatabase(mongoClient.getDatabase("News_Recommendation"));
            userMainMenuController.setUserInfo(currentUserId, currentSessionId); // Pass user and session info

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(userMainMenuScene);
            currentStage.setTitle("User Main Menu");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the main menu.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void userSignHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/homePage.fxml"));
            Scene homeScene = new Scene(loader.load());

            homePage homeController = loader.getController();
            homeController.setMongoClient(mongoClient);
            homeController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(homeScene);
            currentStage.setTitle("Home");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the home page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}
