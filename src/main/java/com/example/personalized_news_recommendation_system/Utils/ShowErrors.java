package com.example.personalized_news_recommendation_system.Utils;

import javafx.scene.control.Alert;

public class ShowErrors {

    public static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
