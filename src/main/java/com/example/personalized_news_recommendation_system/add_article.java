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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class add_article {
    @FXML
    public TextField articleNameField;
    @FXML
    public TextField authorField;
    @FXML
    public DatePicker publishedDatePicker;
    @FXML
    public TextArea contentArea;
    @FXML
    public Button addMainMenu;
    @FXML
    public Button exitArticle;

    private MongoClient mongoClient;
    private MongoDatabase database;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient successfully set in add_article.");
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        this.database = database;
        if (database != null) {
            System.out.println("Connected to database: " + database.getName());
        } else {
            System.out.println("Database is not set in add_article.");
        }
    }

    @FXML
    public void addMainMenu(ActionEvent actionEvent) {
        try {
            // Load the Main Menu page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_main_menu.fxml"));
            Scene mainMenuScene = new Scene(loader.load());

            // Get the Main Menu controller
            administrator_main_menu mainMenuController = loader.getController();

            // Pass MongoDB objects to the Main Menu controller
            if (mongoClient != null && database != null) {
                mainMenuController.setMongoClient(mongoClient);
                mainMenuController.setDatabase(database);
            } else {
                System.out.println("MongoClient or database is null when navigating to Main Menu.");
                showError("Error", "Database connection is not established. Please contact the administrator.");
                return;
            }

            // Set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(mainMenuScene);
            currentStage.setTitle("Main Menu - Personalized News Recommendation System");
            currentStage.show();

        } catch (IOException e) {
            showError("Navigation Error", "Failed to load the Main Menu page.");
            e.printStackTrace();
        }
    }

    @FXML
    public void exitArticle(ActionEvent actionEvent) {
        // Close the application
        showConfirmation("Exit", "Are you sure you want to exit?");
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

    @FXML
    public void submitArticle(ActionEvent actionEvent) {
        String articleName = articleNameField.getText();
        String author = authorField.getText();
        LocalDate publishedDate = publishedDatePicker.getValue();
        String content = contentArea.getText();

        if (articleName.isEmpty() || author.isEmpty() || publishedDate == null || content.isEmpty()) {
            showError("Error", "All fields must be filled out.");
            return;
        }

        List<String> categories = categorizeArticle(content);

        if (database == null) {
            showError("Error", "Database is not set.");
            return;
        }

        MongoCollection<Document> articlesCollection = database.getCollection("Articles");
        Document article = new Document()
                .append("articleName", articleName)
                .append("author", author)
                .append("publishedDate", publishedDate.toString())
                .append("content", content)
                .append("categories", categories);

        articlesCollection.insertOne(article);

        showSuccess("Success", "Article added successfully!");
        clearFields();
    }

    // Utility method to show error alerts
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Utility method to show success alerts
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Utility method to show confirmation alerts
    private void showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to categorize article based on content keywords
    private List<String> categorizeArticle(String content) {
        Map<String, String> keywordCategoryMap = new HashMap<>();
        keywordCategoryMap.put("ai", "AI");
        keywordCategoryMap.put("artificial", "AI");
        keywordCategoryMap.put("intelligence", "AI");
        keywordCategoryMap.put("technology", "Technology");
        keywordCategoryMap.put("innovation", "Technology");
        keywordCategoryMap.put("health", "Health");
        keywordCategoryMap.put("fitness", "Health");
        keywordCategoryMap.put("medicine", "Health");
        keywordCategoryMap.put("education", "Education");
        keywordCategoryMap.put("school", "Education");
        keywordCategoryMap.put("learning", "Education");
        keywordCategoryMap.put("fashion", "Fashion");
        keywordCategoryMap.put("style", "Fashion");
        keywordCategoryMap.put("clothing", "Fashion");
        keywordCategoryMap.put("sports", "Sports");
        keywordCategoryMap.put("football", "Sports");
        keywordCategoryMap.put("cricket", "Sports");
        keywordCategoryMap.put("entertainment", "Entertainment");
        keywordCategoryMap.put("movie", "Entertainment");
        keywordCategoryMap.put("music", "Entertainment");

        Set<String> categories = new HashSet<>();
        String[] words = content.toLowerCase().split("\\W+");
        for (String word : words) {
            if (keywordCategoryMap.containsKey(word)) {
                categories.add(keywordCategoryMap.get(word));
            }
        }

        return new ArrayList<>(categories);
    }

    // Clears the fields after article submission
    private void clearFields() {
        articleNameField.clear();
        authorField.clear();
        publishedDatePicker.setValue(null);
        contentArea.clear();
    }
}
