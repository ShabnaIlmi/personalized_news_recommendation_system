package com.example.personalized_news_recommendation_system.Controller.AdminController;

import com.example.personalized_news_recommendation_system.Utils.ShowAlerts;
import com.example.personalized_news_recommendation_system.Utils.ShowErrors;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    @FXML
    private Button fetchArticle;
    @FXML
    private StackPane progressContainer;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label progressMessage;

    private MongoClient mongoClient;
    private MongoCollection<Document> articlesCollection;

    // ExecutorService for concurrency
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    // Setting the MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setting the MongoDatabase and initializing the articles collection
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
        } else {
            ShowErrors.showError("Database Initialization Error", "Database is null. Cannot initialize articles collection.");
        }
    }

    //Submitting the articles manually
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

            // Check for duplicate article ID
            if (isDuplicateArticle(id)) {
                ShowAlerts.showAlert("Validation Error", "An article with the same Article ID already exists. Please use a unique ID.", Alert.AlertType.ERROR);
                return;
            }

            // Check for duplicate content
            if (isDuplicateContent(content)) {
                ShowAlerts.showAlert("Validation Error", "An article with the same content already exists. Please modify the content or use a different article.", Alert.AlertType.ERROR);
                return;
            }

            // Predict the category using Hugging Face in a separate thread
            Future<String> categoryFuture = executorService.submit(() -> predictCategory(content, labels));

            // Get the category (this will block until the result is available)
            String category = categoryFuture.get();

            // If the category is not one of the predefined ones, classify it as "General"
            if (!isValidCategory(category)) {
                category = "General";
            }

            // Get the current timestamp in ISO 8601 format
            String articleAddedTime = getCurrentTimestamp();

            // Create and insert the article document into MongoDB
            Document article = createArticleDocument(id, title, author, publishedDate, articleAddedTime, articleDescription, content, category);
            articlesCollection.insertOne(article);

            // Show success message
            String finalCategory = category;
            Platform.runLater(() -> ShowAlerts.showAlert("Success", "Article added successfully under category: " + finalCategory, Alert.AlertType.INFORMATION));

            // Clear input fields after submission
            clearFields();
        } catch (IllegalStateException e) {
            ShowErrors.showError("Database Error", e.getMessage());
        } catch (Exception e) {
            ShowErrors.showError("Unexpected Error", e.getMessage());
        }
    }

    // Check for duplicate articles by content
    private boolean isDuplicateContent(String content) {
        return articlesCollection.find(new Document("content", content)).first() != null;
    }


    // Validation for the MongoDB collection
    private void validateMongoCollection() {
        if (articlesCollection == null) {
            throw new IllegalStateException("Articles collection not initialized.");
        }
    }

    // Validating input fields
    private void validateInputFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.isEmpty()) {
                throw new IllegalArgumentException("All fields must be filled before submitting.");
            }
        }
    }

    // Check for duplicate articles by ID
    private boolean isDuplicateArticle(String id) {
        return articlesCollection.find(new Document("article_id", id)).first() != null;
    }

    private final String[] labels = {"AI", "Technology", "Education", "Health", "Sports", "Fashion", "Entertainment", "Environment", "General"};

    // Category Prediction using Hugging Face API
    private String predictCategory(String content, String[] labels) throws Exception {
        String apiUrl = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
        String token = "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";

        String payload = constructPayload(content, labels);

        // Send request and parse response
        return sendCategoryPredictionRequest(apiUrl, token, payload);
    }

    // Construct JSON payload for API request
    private String constructPayload(String content, String[] labels) {
        // Convert labels array to a JSON array string format for candidate_labels
        String candidateLabelsJson = "[\"" + String.join("\", \"", labels) + "\"]";

        // Construct the JSON payload
        return String.format("{\"inputs\": \"%s\", \"parameters\": {\"candidate_labels\": %s}}",
                content.replace("\"", "\\\""),
                candidateLabelsJson);
    }


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

        // Check the HTTP response code
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            // Read error stream
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line.trim());
                }
                throw new Exception("HTTP Error: " + responseCode + " - " + errorResponse.toString());
            }
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

    // Validate if the category is one of the predefined labels
    private boolean isValidCategory(String category) {
        for (String label : labels) {
            if (label.equals(category)) {
                return true;
            }
        }
        return false;
    }

    // Get current timestamp in ISO 8601 format
    private String getCurrentTimestamp() {
        return Instant.now()
                .atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    // Create MongoDB document for the article
    private Document createArticleDocument(String id, String title, String author, String publishedDate, String articleAddedTime, String description, String content, String category) {
        return new Document("article_id", id)
                .append("article_name", title)
                .append("author", author)
                .append("published_date", publishedDate)
                .append("article_added_time", articleAddedTime)
                .append("description", description)
                .append("content", content)
                .append("category", category);
    }

    @FXML
    public void fetchArticle() {
        // Configure file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        // Open file chooser and get the selected file
        File selectedFile = fileChooser.showOpenDialog(fetchArticle.getScene().getWindow());

        // If no file is selected, return early
        if (selectedFile == null) {
            return;
        }

        // Show progress indicator and message
        Platform.runLater(() -> {
            progressMessage.setText("Loading articles... Please wait.");
            progressContainer.setVisible(true);
        });

        // Process file in a separate thread
        executorService.submit(() -> {
            try {
                // Read the content of the file into a string
                String content = new String(Files.readAllBytes(selectedFile.toPath()), StandardCharsets.UTF_8);

                // Parse the file content as a JSONArray
                JSONArray articlesArray = new JSONArray(content);

                for (int i = 0; i < articlesArray.length(); i++) {
                    JSONObject articleJson = articlesArray.getJSONObject(i);

                    // Extract fields from JSON
                    String id = articleJson.getString("article_id");
                    String title = articleJson.getString("article_name");
                    String author = articleJson.getString("author");
                    String publishedDate = articleJson.optString("published_date", "");
                    String description = articleJson.getString("description");
                    String contentText = articleJson.getString("content");

                    // Check for duplicate article ID
                    if (isDuplicateArticle(id)) {
                        int currentIndex = i + 1;
                        Platform.runLater(() -> ShowAlerts.showAlert(
                                "Validation Error",
                                "Article ID already exists. Skipping article #" + currentIndex + ": " + title,
                                Alert.AlertType.WARNING
                        ));
                        continue;
                    }

                    // Predict category
                    String category = predictCategory(contentText, labels);

                    // Default to "General" if category is not valid
                    if (!isValidCategory(category)) {
                        category = "General";
                    }

                    // Generate timestamp and create article document
                    String articleAddedTime = getCurrentTimestamp();
                    Document article = createArticleDocument(id, title, author, publishedDate, articleAddedTime, description, contentText, category);

                    // Insert into MongoDB
                    articlesCollection.insertOne(article);
                }

                // Notify success
                Platform.runLater(() -> ShowAlerts.showAlert(
                        "Success",
                        "All valid articles were fetched, categorized, and inserted successfully.",
                        Alert.AlertType.INFORMATION
                ));

            } catch (IOException e) {
                // Handle file read errors
                Platform.runLater(() -> ShowErrors.showError(
                        "File Error",
                        "Error reading the file: " + e.getMessage()
                ));
            } catch (Exception e) {
                // Handle other errors
                Platform.runLater(() -> ShowErrors.showError(
                        "Processing Error",
                        "Error processing the file: " + e.getMessage()
                ));
            } finally {
                // Hide progress indicator
                Platform.runLater(() -> progressContainer.setVisible(false));
            }
        });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/administrator_main_menu.fxml"));
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
            ShowAlerts.showAlert("Error", "Failed to open Main Menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Handle the "Exit" button action
    @FXML
    public void exitArticle() {
        Stage stage = (Stage) articleNameField.getScene().getWindow();
        stage.close();
    }
}