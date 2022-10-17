package edu.handong.csee.isel.data.collector.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import java.io.IOException;

/**
 * Wrtier that writes bfc properties in PySZZ json format.
*/
public class PropertyWriter {
    public static String ORGANIZATION = "apache";
    public static String LANGUAGE = "java";
    private PrintWriter out;

    /**
     * @param fileName file name
     * @throws IOException
     */
    public PropertyWriter(String fileName) throws IOException {
        out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
    }

    /**
     * Writes full project name and fix commit hashes in PySZZ json format. 
     * @param projects name of the projects
     * @param hashes hash codes of bfc commits
     */
    public void writePySZZJson(String[] projects, ArrayList<String>[] hashes) {
        out.print("[\n");

        for (int i = 0; i < hashes.length; i++) {
            for (int j = 0; j < hashes[i].size(); j++) {
                out.print("\t{\n");
                out.printf("\t\t\"repo_name\": \"%s/%s\",\n", 
                           ORGANIZATION, projects[i]);
                out.printf("\t\t\"fix_commit_hash\": \"%s\",\n", 
                           hashes[i].get(j));
                out.printf("\t\t\"language\": [\"%s\"]\n", LANGUAGE);
                out.print("\t}");
                
                if (i == hashes.length - 1 && j == hashes[i].size() - 1) {
                    out.print("\n");
                } else {
                    out.print(",\n");        
                }
            }
        }
        out.print("]\n");
        out.close();  
    }
}
