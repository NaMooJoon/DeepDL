package edu.handong.csee.isel.data.collector.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class that makes dataset for DeepDL model.
 */
public class DatasetMaker {

    /**
     * Makes clean snapshots of the given repository with bic before the given splitting point and writes commit JSON files of the given repository with bic after the given splitting point.
     * The commit JSON files will be written at <code>projectPath</code>/out/test-data 
     * @param repository the repository
     * @param splittingPoint the splitting point
     */
    public void makeDataset(String repository, Date splittingPoint) {
        



    }

    /**
     * Removes the given line from the given file. 
     * @param filename the filename
     * @param line the line
     * @throws IOException 
     */
    private void removeLine(String filename, String line) 
            throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filename));
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(filename)));
        
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).equals(line)) {
                out.write(lines.get(i));
            }
        }
    } 

    /**
     * 
     */
    //private 
}
