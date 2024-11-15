package com.example.personalized_news_recommendation_system;

import java.util.*;

public class KeywordExtractor {
    private final Map<String, List<String>> categoryKeywords;

    public KeywordExtractor() {
        categoryKeywords = new HashMap<>();
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

        return categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Uncategorized");
    }
}
