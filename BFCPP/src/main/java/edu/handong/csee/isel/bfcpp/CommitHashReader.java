package edu.handong.csee.isel.bfcpp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *  Reads commit hashes from DPMiner-produced PATCH file.
 */
public class CommitHashReader {
    final int COMMIT_HASH_COLUMN = 2;
    final int FIX_DATE_COLUMN = 4;

    /**
     * Reads commit hashes from <code>startDate</code>(inclusive) to <code>endDate</code>(exclusive).
     * Duplicate hashes are removed.
     * The file should follow DPMiner-produced PATCH file format.
     * @param fileName filename to read
     * @param startDate start date 
     * @param endDate end date
     * @return BFC hashes
     */
    public ArrayList<String> read(String fileName, String startDate, String endDate) {     
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            ArrayList<String> hashes = new ArrayList<>();
            String line = reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] splited = line.split(",");

                if (startDate.compareTo(splited[FIX_DATE_COLUMN - 1]) <= 0 
                        && endDate.compareTo(splited[FIX_DATE_COLUMN - 1]) > 0
                        && hashes.size() != 0 
                        && !hashes.get(hashes.size() - 1)
                                  .equals(splited[COMMIT_HASH_COLUMN - 1]))
                    hashes.add(splited[COMMIT_HASH_COLUMN - 1]);    
            }
            reader.close();

            return hashes;
        } catch (FileNotFoundException e) {
            e.printStackTrace(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}