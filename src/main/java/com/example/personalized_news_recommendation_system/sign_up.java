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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class sign_up implements Initializable {
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

        if (category1.getValue() == null || category2.getValue() == null || category3.getValue() == null ||
                new HashSet<>(Arrays.asList(category1.getValue(), category2.getValue(), category3.getValue())).size() < 3) {
            showAlert("Registration Failed", "Please select three distinct categories.", Alert.AlertType.ERROR);
            return;
        }

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
                .append("password", password); // Consider hashing passwords for security

        try {
            userCollection.insertOne(newUser);

            showAlert("Registration Successful",
                    "Your account has been created!\nUsername: " + username + "\nPassword: " + password,
                    Alert.AlertType.INFORMATION);

            navigateToMainMenu(actionEvent);
        } catch (Exception e) {
            showAlert("Database Error", "An error occurred while saving to the database.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName.toLowerCase() + capitalize(lastName);

        String regex = "^" + Pattern.quote(baseUsername) + "(\\d{3})@swiftly\\.com$";
        Pattern usernamePattern = Pattern.compile(regex);

        int maxSuffix = 0;

        for (Document user : userCollection.find(new Document("username", new Document("$regex", regex)))) {
            String username = user.getString("username");
            Matcher matcher = usernamePattern.matcher(username);

            if (matcher.find()) {
                int suffix = Integer.parseInt(matcher.group(1));
                if (suffix > maxSuffix) {
                    maxSuffix = suffix;
                }
            }
        }

        int newSuffix = maxSuffix + 1;
        return baseUsername + String.format("%03d", newSuffix) + "@swiftly.com";
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return "";
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainMenu.fxml"));
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
    public void initialize(URL url, ResourceBundle resourceBundle) {
        category1.getItems().addAll(categories);
        category2.getItems().addAll(categories);
        category3.getItems().addAll(categories);
    }

    @FXML
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
        }
    }

    @FXML
    public void category1(MouseEvent mouseEvent) {
    }

    @FXML
    public void category2(MouseEvent mouseEvent) {
    }

    @FXML
    public void category3(MouseEvent mouseEvent) {
    }
}
