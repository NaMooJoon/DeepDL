package edu.handong.csee.isel.bfcpp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *  Reads issue number from DPMiner-produced Jira BFC file.
 */
public class IssueNumberReader {

    /**
     * @param fileName filename to read
     * @return ArrayList of issue number
     */
    public ArrayList<String> read(String fileName) {     
        try {
            final int ISSUE_NUMBER_LOCATION = 3;
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            ArrayList<String> issueNums = new ArrayList<>();
            String line = reader.readLine();
           
            while ((line = reader.readLine()) != null) {
                issueNums.add(line.split(",")[ISSUE_NUMBER_LOCATION - 1]);    
            }
            reader.close();

            return issueNums;
        } catch (FileNotFoundException e) {
            e.printStackTrace(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}