package com.example.personalized_news_recommendation_system;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PosTaggerHelper {
    private POSTaggerME posTagger;

    public PosTaggerHelper(String modelPath) throws IOException {
        try (InputStream modelIn = new FileInputStream(modelPath)) {
            POSModel posModel = new POSModel(modelIn);
            posTagger = new POSTaggerME(posModel);
        }
    }

    public String[] posTagTokens(String[] tokens) {
        return posTagger.tag(tokens);
    }
}

