package com.example.personalized_news_recommendation_system;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NERHelper {
    private NameFinderME nameFinder;

    public NERHelper(String modelPath) throws IOException {
        if (modelPath == null || modelPath.trim().isEmpty()) {
            throw new IllegalArgumentException("NER model path cannot be null or empty.");
        }

        try (InputStream modelIn = new FileInputStream(modelPath)) {
            TokenNameFinderModel nerModel = new TokenNameFinderModel(modelIn);
            nameFinder = new NameFinderME(nerModel);
        }
    }

    public List<String> findEntities(String[] tokens) {
        if (tokens == null || tokens.length == 0) {
            throw new IllegalArgumentException("Tokens array cannot be null or empty.");
        }

        Span[] spans = nameFinder.find(tokens);
        List<String> entities = new ArrayList<>();

        for (Span span : spans) {
            StringBuilder entity = new StringBuilder();
            for (int i = span.getStart(); i < span.getEnd(); i++) {
                entity.append(tokens[i]).append(" ");
            }
            entities.add(entity.toString().trim());
        }

        return entities;
    }
}
