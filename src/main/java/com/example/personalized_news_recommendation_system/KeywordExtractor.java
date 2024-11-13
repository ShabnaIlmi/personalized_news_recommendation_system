package com.example.personalized_news_recommendation_system;

import java.util.*;

public class KeywordExtractor {
    private Map<String, List<String>> categoryKeywords;

    public KeywordExtractor() {
        categoryKeywords = new HashMap<>();
        // Example predefined categories and their associated keywords
        categoryKeywords.put("Technology", Arrays.asList("AI", "Machine Learning", "Computing"));
        categoryKeywords.put("Health", Arrays.asList("Healthcare", "Medicine", "Wellness"));
        categoryKeywords.put("Sports", Arrays.asList("Football", "Basketball", "Olympics"));
    }

    public String categorizeArticle(String[] keywords) {
        Map<String, Integer> categoryScores = new HashMap<>();

        for (String keyword : keywords) {
            for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
                if (entry.getValue().contains(keyword)) {
                    categoryScores.put(entry.getKey(), categoryScores.getOrDefault(entry.getKey(), 0) + 1);
                }
            }
        }

        // Find the category with the highest score
        return categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Uncategorized");
    }
}

