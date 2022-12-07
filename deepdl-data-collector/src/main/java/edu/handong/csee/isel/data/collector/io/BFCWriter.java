package edu.handong.csee.isel.data.collector.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Wrtier that writes bfc properties in PySZZ json format.
*/
public class BFCWriter {
    private static final String LANGUAGE = "java";
    private PrintWriter out;

    /**
     * @param fileName file name
     * @throws IOException
     */
    public BFCWriter(String fileName) throws IOException {
        out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
    }

    /**
     * Writes full project name and bfc commit hashes of the given repository in PySZZ json format. 
     * @param repouser the repouser of the repository
     * @param repository the name of the repository
     * @param hashes the bfc commit hashes of the repository
     */
    public void writeBFC(String repouser, String repository, 
                         ArrayList<String> hashes) {
        out.print("[\n");
        
        for (int i = 0; i < hashes.size(); i++) {
            out.print("\t{\n");
            out.printf("\t\t\"repo_name\": \"%s\",\n", 
                        repouser + "/" + repository);
            out.printf("\t\t\"fix_commit_hash\": \"%s\",\n", 
                        hashes.get(i));
            out.printf("\t\t\"language\": [\"%s\"]\n", LANGUAGE);
            out.print("\t}");
            
            if (i == hashes.size() - 1) {
                out.print("\n");
            } else {
                out.print(",\n");        
            }      
        }
        out.print("]\n");
        out.flush();  
    }

    /**
     * closes this instance.
     */
    public void close() {
        out.close();
    }
}
