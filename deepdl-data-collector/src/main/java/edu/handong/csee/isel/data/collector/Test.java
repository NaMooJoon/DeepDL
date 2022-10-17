package edu.handong.csee.isel.data.collector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        try {
            String[] projects = { "juddi" };
            ArrayList<String>[] hashes = new ArrayList[1];

            hashes[0] = new CommitHashReader("C:/PATCH_juddi.csv")
                    .readCommitHashes("2004-01-01", "2022-12-31");
    
            new PropertyWriter("test.json").writePySZZJson(projects, hashes);

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
