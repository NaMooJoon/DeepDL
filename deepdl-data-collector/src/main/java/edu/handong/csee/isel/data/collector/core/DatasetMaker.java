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

/**
 * Class that makes dataset for DeepDL model.
 */
public class DatasetMaker {

    /**
     * Makes clean snapshots with bfc before the splitting point and writes commit files and pinpointed buggy lines file with bfc after the splitting point.
     * The commit files will be written at <code>projectPath</code>/out/test-data/commits.
     * The pinpointed buggy lines file will be written at <code>projectPath</code>/out/test-data/buggy-lines. 
     * @param commits the bfc commits and their corresponding bic commits
     * @param splittingPoint the splitting point
     */
    public void makeDataset(JSONArray commits, Date splittingPoint) {
                

    }

    /**
     * Removes the given buggy line from the given file. 
     * @param filename filename
     * @param buggyLine buggy line
     * @throws IOException 
     */
    private void removeBuggyLine(String filename, String buggyLine) 
            throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filename));
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(filename)));
        
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).equals(buggyLine)) {
                out.write(lines.get(i));
            }
        }
    } 
}
