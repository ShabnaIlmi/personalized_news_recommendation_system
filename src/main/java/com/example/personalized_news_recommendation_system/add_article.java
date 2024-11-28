package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class add_article {

    @FXML
    private TextField articleID;
    @FXML
    private TextField articleNameField;
    @FXML
    private TextField authorField;
    @FXML
    private DatePicker publishedDatePicker;
    @FXML
    private TextArea description;
    @FXML
    private TextArea contentArea;

    private MongoClient mongoClient;
    private MongoCollection<Document> articlesCollection;

    // Set the MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in Add Article.");
    }

    // Set the MongoDatabase and initialize the articles collection
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            System.out.println("Articles collection initialized: " + articlesCollection.getNamespace());
        } else {
            showError("Database Initialization Error", "Database is null. Cannot initialize articles collection.");
        }
    }

    // Handle the submit button action
    @FXML
    public void submitArticle() {
        try {
            validateMongoCollection();

            // Get data from the input fields
            String id = articleID.getText();
            String title = articleNameField.getText();
            String author = authorField.getText();
            String publishedDate = (publishedDatePicker.getValue() != null) ? publishedDatePicker.getValue().toString() : "";
            String articleDescription = description.getText();
            String content = contentArea.getText();

            // Validate input fields
            validateInputFields(id, title, author, publishedDate, articleDescription, content);

            // Check for duplicate articleID
            if (isDuplicateArticle(id)) {
                showAlert("Validation Error", "An article with the same Article ID already exists. Please use a unique ID.", Alert.AlertType.ERROR);
                return;
            }

            // Predict the category using Hugging Face
            String category = predictCategory(content, labels);

            // Get the current timestamp in ISO 8601 format
            String articleAddedTime = getCurrentTimestamp();

            // Create and insert the article document into MongoDB
            Document article = createArticleDocument(id, title, author, publishedDate, articleAddedTime, articleDescription, content, category);
            articlesCollection.insertOne(article);

            // Show success message
            Platform.runLater(() -> showAlert("Success", "Article added successfully under category: " + category, Alert.AlertType.ERROR));

            // Clear input fields after submission
            clearFields();
        } catch (IllegalStateException e) {
            showError("Database Error", e.getMessage());
        } catch (Exception e) {
            showError("Unexpected Error", e.getMessage());
        }
    }

    // Validate the MongoDB collection
    private void validateMongoCollection() {
        if (articlesCollection == null) {
            throw new IllegalStateException("Articles collection not initialized.");
        }
    }

    // Validate input fields
    private void validateInputFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.isEmpty()) {
                throw new IllegalArgumentException("All fields must be filled before submitting.");
            }
        }
    }

    // Check for duplicate articles by ID
    private boolean isDuplicateArticle(String id) {
        return articlesCollection.find(new Document("articleID", id)).first() != null;
    }

    private final String[] labels = {"politics", "technology", "health", "business", "education"};
    // Predict the category using Hugging Face API
    private String predictCategory(String content, String[] labels) throws Exception {
        String apiUrl = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
        String token = "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";

        String payload = constructPayload(content, labels);

        // Send request and parse response
        return sendCategoryPredictionRequest(apiUrl, token, payload);
    }

    // Construct JSON payload for API request
    private String constructPayload(String content, String[] labels) {
        return String.format("{\"inputs\": \"%s\", \"parameters\": {\"candidate_labels\": [\"%s\"]}}",
                content.replace("\"", "\\\""),
                String.join("\", \"", labels));
    }

    // Send category prediction request and parse the response
    private String sendCategoryPredictionRequest(String apiUrl, String token, String payload) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Write payload
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return parseCategoryFromResponse(response.toString());
    }

    // Parse category from API response using org.json
    private String parseCategoryFromResponse(String response) throws Exception {
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray labelsArray = jsonResponse.optJSONArray("labels");

        if (labelsArray == null || labelsArray.length() == 0) {
            throw new Exception("Response does not contain valid 'labels' field.");
        }

        // Return the top label
        return labelsArray.getString(0);
    }

    // Get current timestamp in ISO 8601 format
    private String getCurrentTimestamp() {
        return Instant.now()
                .atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    // Create MongoDB document for the article
    private Document createArticleDocument(String id, String title, String author, String publishedDate, String articleAddedTime, String description, String content, String category) {
        return new Document("articleID", id)
                .append("articleName", title)
                .append("author", author)
                .append("publishedDate", publishedDate)
                .append("article_added_time", articleAddedTime)
                .append("description", description)
                .append("content", content)
                .append("category", category);
    }

    // Show an alert dialog
    private void showAlert(String title, String content, Alert.AlertType error) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Show an error dialog
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Clear input fields
    private void clearFields() {
        articleID.clear();
        articleNameField.clear();
        authorField.clear();
        description.clear();
        contentArea.clear();
        publishedDatePicker.setValue(null);
    }

    // Handle the "Main Menu" button action
    @FXML
    public void addMainMenu(ActionEvent actionEvent) {
        try {
            // Close the current stage
            Stage currentStage = (Stage) articleNameField.getScene().getWindow();
            currentStage.close();

            // Load the administrator main menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_main_menu.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the controller of the new FXML
            administrator_main_menu controller = loader.getController();

            // Pass the mongoClient, database, and any other required information to the new controller
            controller.setMongoClient(mongoClient);
            controller.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Set up the new stage and show the main menu
            Stage mainMenuStage = new Stage();
            mainMenuStage.setScene(scene);
            mainMenuStage.setTitle("Main Menu");
            mainMenuStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open Main Menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Handle the "Exit" button action
    @FXML
    public void exitArticle() {
        Stage stage = (Stage) articleNameField.getScene().getWindow();
        stage.close();
    }
}