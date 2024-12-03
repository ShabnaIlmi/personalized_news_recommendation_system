package com.example.personalized_news_recommendation_system.Controller.UserController;

import com.example.personalized_news_recommendation_system.Utils.ShowAlerts;
import com.example.personalized_news_recommendation_system.Utils.Validator;
import com.example.personalized_news_recommendation_system.Driver.homePage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDateTime;

public class user_sign_up {

    @FXML
    private TextField firstName;
    @FXML
    private TextField secondName;
    @FXML
    private TextField email;
    @FXML
    private TextField age;
    @FXML
    private PasswordField createPassword;
    @FXML
    private PasswordField verifyPassword;
    @FXML
    private ChoiceBox<String> category1;
    @FXML
    private ChoiceBox<String> category2;
    @FXML
    private ChoiceBox<String> category3;
    @FXML
    private Button signUp;
    @FXML
    private Button homeSignUp;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection;
    private MongoCollection<Document> userLogCollection;
    private String currentUserId;
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
    public void initialize() {
        ObservableList<String> categories = FXCollections.observableArrayList(
                "AI", "Technology", "Education", "Health", "Sports", "Fashion", "Entertainment", "Environment", "General"
        );
        category1.setItems(categories);
        category2.setItems(categories);
        category3.setItems(categories);
    }

    @FXML
    public void signUp(ActionEvent event) {
        String firstNameText = firstName.getText().trim();
        String secondNameText = secondName.getText().trim();
        String emailText = email.getText().trim();
        String ageText = age.getText().trim();
        String password = createPassword.getText().trim();
        String verifyPasswordText = verifyPassword.getText().trim();

        if (firstNameText.isEmpty() || secondNameText.isEmpty() || emailText.isEmpty() || ageText.isEmpty() || password.isEmpty() || verifyPasswordText.isEmpty()) {
            ShowAlerts.showAlert("Input Error", "Please fill in all the fields.", Alert.AlertType.ERROR);
            return;
        }

        if (!Validator.isValidEmail(emailText)) {
            ShowAlerts.showAlert("Email Error", "Please enter a valid email address.", Alert.AlertType.ERROR);
            return;
        }

        if (!Validator.isValidAge(ageText)) {
            ShowAlerts.showAlert("Age Error", "Please enter a valid age (a positive number).", Alert.AlertType.ERROR);
            return;
        }

        if (!Validator.isValidPassword(password)) {
            ShowAlerts.showAlert("Password Error", "Password must be at least 8 characters long, contain an uppercase letter, a lowercase letter, and a number.", Alert.AlertType.ERROR);
            return;
        }

        String category1Value = category1.getValue();
        String category2Value = category2.getValue();
        String category3Value = category3.getValue();

        if (category1Value == null || category2Value == null || category3Value == null) {
            ShowAlerts.showAlert("Category Error", "Please select all three categories.", Alert.AlertType.ERROR);
            return;
        }

        // Validate distinct categories
        if (!Validator.areCategoriesDistinct(category1Value, category2Value, category3Value)) {
            ShowAlerts.showAlert("Category Error", "The categories must be distinct. Please select different categories.", Alert.AlertType.ERROR);
            return;
        }

        String generatedUsername = generateUsername(firstNameText, secondNameText);

        if (isUsernameTaken(generatedUsername)) {
            ShowAlerts.showAlert("Sign-Up Error", "Username already exists. Please try again.", Alert.AlertType.ERROR);
            return;
        }

        Document newUser = new Document("first_name", firstNameText)
                .append("last_name", secondNameText)
                .append("email", emailText)
                .append("age", Integer.parseInt(age.getText()))
                .append("categories", FXCollections.observableArrayList(category1Value, category2Value, category3Value))
                .append("username", generatedUsername)
                .append("password", password)
                .append("created_date_time", LocalDateTime.now().toString());
        userCollection.insertOne(newUser);

        logUserSignUp(generatedUsername);

        String sessionId = generateSessionId(generatedUsername);

        currentUserId = generatedUsername;
        currentSessionId = sessionId;

        // Welcome Message
        String welcomeMessage = "Welcome, " + firstNameText + " " + secondNameText + "!\n" +
                "You have successfully signed up.\n" +
                "Your username is: " + generatedUsername + "\n" +
                "Enjoy personalized news recommendations!";
        ShowAlerts.showAlert("Welcome", welcomeMessage, Alert.AlertType.INFORMATION);

        navigateToMainMenu(event);
    }

    private String generateUsername(String firstName, String lastName) {
        String lowerFirstName = firstName.toLowerCase();
        String camelLastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
        String baseUsername = lowerFirstName + camelLastName;
        String uniqueNumber = generateUniqueNumber(baseUsername);
        return baseUsername + uniqueNumber + "@swiftly.com";
    }

    private String generateUniqueNumber(String username) {
        int count = (int) userCollection.countDocuments(new Document("username", new Document("$regex", "^" + username)));
        return String.format("%03d", count + 1);
    }

    private boolean isUsernameTaken(String username) {
        return userCollection.find(new Document("username", username)).first() != null;
    }

    private void logUserSignUp(String username) {
        String sessionId = generateSessionId(username);

        Document log = new Document("user_id", username)
                .append("session_id", sessionId)
                .append("action", "Sign-Up")
                .append("logged_date_time", LocalDateTime.now().toString());
        userLogCollection.insertOne(log);
    }

    private String generateSessionId(String username) {
        Document lastSessionDoc = userLogCollection.find(new Document("user_id", username))
                .sort(new Document("logged_date_time", -1)).first();

        int lastSessionNumber = 0;
        if (lastSessionDoc != null) {
            String lastSessionId = lastSessionDoc.getString("session_id");
            lastSessionNumber = Integer.parseInt(lastSessionId);
        }

        return String.format("%03d", lastSessionNumber + 1);
    }

    private void navigateToMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/user_main_menu.fxml"));
            Scene userMainMenuScene = new Scene(loader.load());

            user_main_menu userMainMenuController = loader.getController();
            userMainMenuController.setMongoClient(mongoClient);
            userMainMenuController.setDatabase(mongoClient.getDatabase("News_Recommendation"));
            userMainMenuController.setUserInfo(currentUserId, currentSessionId);

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(userMainMenuScene);
            currentStage.setTitle("User Main Menu");
            currentStage.show();
        } catch (IOException e) {
            ShowAlerts.showAlert("Navigation Error", "Failed to load the main menu.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void homeSignUp(ActionEvent event) {
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
            ShowAlerts.showAlert("Navigation Error", "Failed to load the home page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

}
