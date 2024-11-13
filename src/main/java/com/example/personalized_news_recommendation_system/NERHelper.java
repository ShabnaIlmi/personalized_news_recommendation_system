package com.example.personalized_news_recommendation_system;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NERHelper {
    private NameFinderME nameFinder;

    public NERHelper(String modelPath) throws IOException {
        try (InputStream modelIn = new FileInputStream(modelPath)) {
            TokenNameFinderModel nerModel = new TokenNameFinderModel(modelIn);
            nameFinder = new NameFinderME(nerModel);
        }
    }

    public Span[] findNames(String[] tokens) {
        return nameFinder.find(tokens);
    }
}

