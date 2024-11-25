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
    private String currentSessionId;  // Store the session ID

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

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }

        if (authenticateUser(username, password)) {
            logUserLogin(username);  // Log the user login event
            showAlert("Sign-In Success", "Welcome, " + username + "!", Alert.AlertType.INFORMATION);
            navigateToMainMenu(event);
        } else {
            showAlert("Sign-In Failed", "Invalid credentials.", Alert.AlertType.ERROR);
        }
    }

    private boolean authenticateUser(String username, String password) {
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            String storedPassword = userDoc.getString("password");
            return storedPassword != null && storedPassword.equals(password);
        }
        return false;
    }

    private void logUserLogin(String username) {
        // Generate session ID for this login
        currentUserId = username;
        currentSessionId = generateSessionId(username);

        Document log = new Document("user_id", currentUserId)
                .append("session_id", currentSessionId)  // Store session ID for the login
                .append("action", "Log-In")
                .append("logged_date_time", LocalDateTime.now().toString());
        userLogCollection.insertOne(log);
    }

    private String generateSessionId(String username) {
        // Logic to generate a session ID (incremental, starting from "001")
        Document lastSessionDoc = userLogCollection.find(new Document("user_id", username))
                .sort(new Document("logged_date_time", -1)).first();

        int lastSessionNumber = 0;
        if (lastSessionDoc != null) {
            String lastSessionId = lastSessionDoc.getString("session_id");
            lastSessionNumber = Integer.parseInt(lastSessionId);
        }

        return String.format("%03d", lastSessionNumber + 1);  // Increment session number
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
            currentStage.setTitle("User Main Menu");
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
            currentStage.setTitle("Home");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the home page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}
