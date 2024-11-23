package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class user_sign_up implements Initializable {
    @FXML
    private Button homeSignUp, signUp;
    @FXML
    private TextField firstName, secondName, email, age;
    @FXML
    private PasswordField createPassword, verifyPassword;
    @FXML
    private ChoiceBox<String> category1, category2, category3;

    private MongoClient mongoClient;
    private MongoCollection<Document> userCollection;
    private ExecutorService executorService; // To handle background tasks

    private static final String[] categories = {"AI", "Technology", "Health", "Education", "Fashion", "Sports", "Entertainment"};
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.userCollection = database.getCollection("User");
        }
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @FXML
    private void signUp(ActionEvent actionEvent) {
        if (mongoClient == null || userCollection == null) {
            showAlert("Database Error", "The database is not connected. Please try again later.", Alert.AlertType.ERROR);
            return;
        }

        String fName = firstName.getText().trim();
        String sName = secondName.getText().trim();
        String userEmail = email.getText().trim();
        String password = createPassword.getText();
        String confirmPassword = verifyPassword.getText();
        String ageValue = age.getText().trim();

        if (!validateInput(fName, sName, userEmail, password, confirmPassword, ageValue)) {
            return;
        }

        String username = generateUsername(fName, sName);
        int userAge = Integer.parseInt(ageValue);

        Document newUser = new Document()
                .append("first_name", fName)
                .append("last_name", sName)
                .append("email", userEmail)
                .append("age", userAge)
                .append("categories", Arrays.asList(category1.getValue(), category2.getValue(), category3.getValue()))
                .append("username", username)
                .append("password", password);

        // Insert new user into the database in a background thread
        executorService.submit(() -> {
            try {
                userCollection.insertOne(newUser);

                Platform.runLater(() -> {
                    showAlert("Registration Successful",
                            "Your account has been created!\nUsername: " + username,
                            Alert.AlertType.INFORMATION);
                    navigateToMainMenu(actionEvent);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Database Error", "An error occurred while saving to the database.", Alert.AlertType.ERROR);
                    e.printStackTrace();
                });
            }
        });
    }

    private String generateUsername(String firstName, String lastName) {
        return firstName.toLowerCase() + lastName.toLowerCase() + "@swiftly.com";
    }

    private boolean validateInput(String fName, String sName, String userEmail, String password, String confirmPassword, String ageValue) {
        if (fName.isEmpty() || sName.isEmpty() || userEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || ageValue.isEmpty()) {
            showAlert("Registration Failed", "All fields are required.", Alert.AlertType.ERROR);
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Registration Failed", "Passwords do not match.", Alert.AlertType.ERROR);
            return false;
        }

        if (!EMAIL_PATTERN.matcher(userEmail).matches()) {
            showAlert("Invalid Email", "Enter a valid email address.", Alert.AlertType.ERROR);
            return false;
        }

        try {
            int age = Integer.parseInt(ageValue);
            if (age <= 0) {
                showAlert("Invalid Age", "Enter a valid age.", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Age", "Age must be a number.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void navigateToMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_main_menu.fxml"));
            Scene mainMenuScene = new Scene(loader.load());

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(mainMenuScene);
            currentStage.setTitle("Main Menu");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the main menu.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        category1.getItems().addAll(categories);
        category2.getItems().addAll(categories);
        category3.getItems().addAll(categories);
    }

    public void homeSignUp(ActionEvent actionEvent) {
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
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred while navigating to the home page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void category1(MouseEvent mouseEvent) {
    }

    public void category2(MouseEvent mouseEvent) {
    }

    public void category3(MouseEvent mouseEvent) {
    }
}
