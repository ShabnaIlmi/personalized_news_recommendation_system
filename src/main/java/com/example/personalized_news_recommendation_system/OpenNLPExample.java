package com.example.personalized_news_recommendation_system;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import java.io.FileInputStream;
import java.io.InputStream;

public class OpenNLPExample {

    TokenizerME tokenizer;
    private POSTaggerME posTagger;
    NameFinderME nameFinder;

    public OpenNLPExample() throws Exception {
        // Load Tokenizer Model
        try (InputStream tokenModelIn = new FileInputStream("path/to/en-token.bin")) {
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            tokenizer = new TokenizerME(tokenModel);
        }

        // Load POS Tagger Model
        try (InputStream posModelIn = new FileInputStream("path/to/en-pos-maxent.bin")) {
            POSModel posModel = new POSModel(posModelIn);
            posTagger = new POSTaggerME(posModel);
        }

        // Load NER Model (e.g., for Persons)
        try (InputStream nerModelIn = new FileInputStream("path/to/en-ner-person.bin")) {
            TokenNameFinderModel nerModel = new TokenNameFinderModel(nerModelIn);
            nameFinder = new NameFinderME(nerModel);
        }
    }
}

