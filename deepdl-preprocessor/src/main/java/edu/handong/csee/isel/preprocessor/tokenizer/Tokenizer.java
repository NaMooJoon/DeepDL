package edu.handong.csee.isel.preprocessor.tokenizer;

import java.io.IOException;
import java.util.ArrayList;

public class Tokenizer {
    
    public void makeDictionary() throws IOException, InterruptedException, IOException {

        if (this instanceof BPE) {
            ((BPE) this).makeDictionary();
        }
        /* The other tokenizer? */
    }

    public void tokenize(ArrayList<String> lineblock) {
         
        if (this instanceof BPE) {
            ((BPE) this).tokenize(lineblock);
        }
    }

    public void tokenize(String source) {
         
        if (this instanceof BPE) {
            ((BPE) this).tokenize(source);
        }
    }

    public void tokenize(String input, String output) {
         
        if (this instanceof BPE) {
            ((BPE) this).tokenize(input, output);
        }
    }

    public void tokenize(String source, String projectDir, String inputFileName) {
         
        if (this instanceof BPE) {
            ((BPE) this).tokenize(source, projectDir, inputFileName);
        }
    }

    public void tokenize() {
         
        if (this instanceof BPE) {
            ((BPE) this).tokenize();
        }
    }
}
