package edu.handong.csee.isel.data.collector.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import edu.handong.csee.isel.data.collector.exception.FileFormatException;

/**
 *  Reader that reads commit hashes from DPMiner-produced PATCH file.
 */
public class CommitHashReader {
    private static final String FIX_DATE_PATTERN = 
            "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
    
    private BufferedReader in;

    /**
     * Creates an empty reader.
     */
    public CommitHashReader() {}

    /**
     * Creates <code>BufferedReader</code> that reads the file.
     * @param file file 
     * @throws FileNotFoundException
     */
    public CommitHashReader(File file) throws FileNotFoundException {
        in = new BufferedReader(new FileReader(file));
    }

    /**
     * Reads commit hashes from [<code>startDate</code>, <code>endDate</code>).
     * Duplicate hashes are removed.
     * @param repository the name of the repository
     * @param startDate yyyy-MM-dd HH:mm:ss format of start date 
     * @param endDate yyyy-MM-dd HH:mm:ss format of end date 
     * @return BFC hashes
     * @throws IOException
     * @throws FileFormatException
     */
    public ArrayList<String> readCommitHashes(String repository, 
            String startDate, String endDate) 
                    throws IOException, FileFormatException {     
        ArrayList<String> hashes = new ArrayList<>();
        
        while (true) {
            String hash = null, fixDate = null;
         
            while (true) {
                String line = in.readLine();
                
                if (line == null) {
                    return hashes;
                }

                String[] splitted = line.split(",");
            
                if (repository.equals(splitted[0])) {
                    hash = splitted[1];
                    
                    break;
                }
            }
             
            while (true) {
                String line = in.readLine();

                if (line == null) {
                    throw new FileFormatException(
                            "Please check the format of the file.");
                }

                String[] splitted = line.split(",");
                
                if (splitted.length > 1 
                        && splitted[1].matches(FIX_DATE_PATTERN)) {
                    fixDate = splitted[1];
                   
                    break;
                }
            }

            if (startDate.compareTo(fixDate) <= 0 
                    && endDate.compareTo(fixDate) > 0) {
                if (hashes.size() == 0 
                        || !hashes.get(hashes.size() - 1).equals(hash)) {
                    hashes.add(hash);    
                }
            }
        }
    }

    /**
     * Changes this instance's file.
     * @param file file 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void changeFile(File file) 
            throws FileNotFoundException, IOException {
        if (in != null) {
            in.close();
        }
        in = new BufferedReader(new FileReader(file));
    }

    /**
     * Closes this instance.
     */
    public void close() throws IOException {
        in.close();
    }
}