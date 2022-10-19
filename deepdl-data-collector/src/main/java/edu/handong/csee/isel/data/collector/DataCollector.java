package edu.handong.csee.isel.data.collector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Collector that collects data for DeepDL model.
 * This class uses DPMiner and PySZZ.
 * All of the exe file path for these program should be in the environment path.
 */
public class DataCollector {
    private String BFCPath;
    
    /**
     * Sets BFCPath of this instance.
     * @param BFCPath path to write BFC file
     */
    public DataCollector(String BFCPath) {
        this.BFCPath = BFCPath;
    }

    /**
     * Extracts BFC and writes it in csv file using DPMiner.
     * @param url a GitHub repository url
     * @param jiraKey a Jira key
     */
    public void extractBFC(String url, String jiraKey) {
        final int NUM_ANCESTORS = 9; 
        final String WINDOWS_COMMAND_FORMAT = 
                "cmd.exe /c DPMiner.bat patch -i %s patch -o %s -ij -jk %s";
        final String LINUX_COMMAND_FORMAT = 
                "sh -c ./DPMiner.bat patch -i %s -o %s -ij -jk %s";

        String pathSeperator = System.getProperty("path.seperator");
        String[] splittedCwd = System.getProperty("user.dir")
                                     .split(pathSeperator);        
        ArrayList<String> splittedDPMinerPath = new ArrayList<>();                            
        String DPMinerPath;
        
        for (int i = 0; i < splittedCwd.length - NUM_ANCESTORS; i++) {
            splittedDPMinerPath.add(splittedCwd[i]);
        }  
        DPMinerPath = String.join(pathSeperator, splittedDPMinerPath) 
                                + pathSeperator + "tools";
        
        try {                    
            String format = System.getProperty("os.name")
                                  .toLowerCase()
                                  .startsWith("windows") 
                                    ? WINDOWS_COMMAND_FORMAT
                                    : LINUX_COMMAND_FORMAT;       
            Process child = 
                    Runtime.getRuntime()
                           .exec(String.format(format, url, BFCPath, jiraKey), 
                                 null, new File(DPMinerPath));
            
            child.waitFor();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void extractBIC(String BICPath) {

    }
}
