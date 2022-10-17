package edu.handong.csee.isel.data.collector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *  Reads commit hashes from DPMiner-produced PATCH file.
 */
public class CommitHashReader {
    private static final String PROJECT_SPLIT_PATTERN = "PATCH_|\\.";
    private static final String FIX_DATE_PATTERN = 
            "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
    private static final int PROJECT_NAME_INDEX = 1;

    private BufferedReader in;
    private String project;

    /**
     * @param fileName file name
     * @throws FileNotFoundException
     */
    public CommitHashReader(String fileName) throws FileNotFoundException {
        in = new BufferedReader(new FileReader(fileName));
        project = fileName.split(PROJECT_SPLIT_PATTERN)[PROJECT_NAME_INDEX];   
    }

    /**
     * Reads commit hashes from <code>startDate</code>(inclusive) to <code>endDate</code>(exclusive).
     * Duplicate hashes are removed.
     * @param startDate YYYY-MM-DD format of start date 
     * @param endDate YYYY-MM-DD format of end date 
     * @return BFC hashes
     * @throws IOException
     */
    public ArrayList<String> readCommitHashes(String startDate, String endDate)
            throws IOException {     
        ArrayList<String> hashes = new ArrayList<>();
        
        while (true) {
            String line = null;
            String hash = null, fixDate = null;
         
            while ((line = in.readLine()) != null) {
                String[] splitted = line.split(",");
            
                if (project.equals(splitted[0])) {
                    hash = splitted[1];
                    
                    break;
                }
            }

            if (line == null) break;
             
            while (true) {
                String[] splitted = in.readLine().split(",");
                
                if (splitted.length > 1 && splitted[1].matches(FIX_DATE_PATTERN)) {
                    fixDate = splitted[1];
                   
                    break;
                }
            }

            if (startDate.compareTo(fixDate) <= 0 
                    && endDate.compareTo(fixDate) > 0
                    && (hashes.size() == 0 
                    || (hashes.size() != 0 
                    && !hashes.get(hashes.size() - 1).equals(hash)))) {
                hashes.add(hash);    
            }
        } 
        in.close();

        return hashes;
    }
}