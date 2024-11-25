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

public class user_sign_up {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button signUpButton;
    @FXML
    private Button signUpHomeButton;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection;
    private MongoCollection<Document> userLogCollection;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.userCollection = database.getCollection("User");
            this.userLogCollection = database.getCollection("User_Logs");
        }
    }

    @FXML
    void handleSignUp(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }

        if (isUsernameTaken(username)) {
            showAlert("Sign-Up Error", "Username already taken. Please choose another.", Alert.AlertType.ERROR);
            return;
        }

        // Add user to the database
        Document newUser = new Document("username", username)
                .append("password", password)
                .append("created_date_time", LocalDateTime.now().toString());
        userCollection.insertOne(newUser);

        // Log the user sign-up action
        logUserSignUp(username);

        showAlert("Sign-Up Success", "Account created successfully! You can now log in.", Alert.AlertType.INFORMATION);
        navigateToLogIn(event);
    }

    private boolean isUsernameTaken(String username) {
        return userCollection.find(new Document("username", username)).first() != null;
    }

    private void logUserSignUp(String username) {
        // Create the session ID (starting from "001" for new users)
        String sessionId = generateSessionId(username);

        Document log = new Document("user_id", username)
                .append("session_id", sessionId)  // Log the session ID
                .append("action", "Sign-Up")
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

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateToLogIn(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_log_in.fxml"));
            Scene logInScene = new Scene(loader.load());

            user_log_in logInController = loader.getController();
            logInController.setMongoClient(mongoClient);
            logInController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(logInScene);
            currentStage.setTitle("User Log In");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the log-in page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHomeNavigation(ActionEvent actionEvent) {
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
