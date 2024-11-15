package com.example.personalized_news_recommendation_system;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PosTaggerHelper {
    private static final Logger logger = Logger.getLogger(PosTaggerHelper.class.getName());
    private POSTaggerME posTagger;

    public PosTaggerHelper(String modelPath) throws IOException {
        if (modelPath == null || modelPath.trim().isEmpty()) {
            throw new IllegalArgumentException("POS model path cannot be null or empty.");
        }

        try (InputStream modelIn = new FileInputStream(modelPath)) {
            POSModel posModel = new POSModel(modelIn);
            posTagger = new POSTaggerME(posModel);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load POS model from: " + "C:\\Users\\HP\\personalized_news_recommendation_system\\src\\main\\resources\\com\\example\\personalized_news_recommendation_system\\en-pos-maxent.bin", e);
            throw e;
        }
    }

    public String[] posTagTokens(String[] tokens) {
        if (tokens == null || tokens.length == 0) {
            throw new IllegalArgumentException("Tokens array cannot be null or empty.");
        }
        return posTagger.tag(tokens);
    }
}
