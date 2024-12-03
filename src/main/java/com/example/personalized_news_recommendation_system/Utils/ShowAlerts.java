package com.example.personalized_news_recommendation_system.Utils;

import javafx.scene.control.Alert;

public class ShowAlerts {
    public static void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
