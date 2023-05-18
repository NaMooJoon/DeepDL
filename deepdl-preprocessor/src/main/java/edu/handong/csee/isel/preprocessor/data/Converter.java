package edu.handong.csee.isel.preprocessor.data;

import java.io.FileNotFoundException;
import java.io.IOException;


public class Converter {

    public void make() throws FileNotFoundException, IOException {

        if (this instanceof Lineblock) {
            ((Lineblock) this).make();
        }

        if (this instanceof Corpus) {
            ((Corpus) this).make();
        }

        if (this instanceof Dataset) {
            ((Dataset) this).make();
        }

    }

    boolean isCommentLine(String line) {
        boolean isComment = false;

        if (line.startsWith("//")) {
            isComment = true;
        }
        else if (line.startsWith("/*")) {
            isComment = true;
        }
        else if (line.startsWith("*")) {
            isComment = true;
        }
        else if (line.isEmpty()) {
            isComment = true;
        }
        
        return isComment;
    }
}