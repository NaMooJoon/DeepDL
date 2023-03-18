package edu.handong.csee.isel.data.collector.util;

import java.io.File;
import java.util.ArrayList;

public class Utils {
    public static final String PROJECT_DIR = "deepdl-data-collector";

    /**
     * Gets this project's absolute root directory path.
     * @return the project path
     */
    public static String getProjectPath() {
        String regex = File.separator.equals("\\") ? "\\\\" : File.separator;
        String[] splittedCwd = System.getProperty("user.dir").split(regex);        
        ArrayList<String> splittedProjectPath = new ArrayList<>();  
        
        for (String file : splittedCwd) {
            splittedProjectPath.add(file);

            if (file.equals(PROJECT_DIR)) {
                break;
            }
        }

        return String.join(File.separator, splittedProjectPath); 
    }
    
}
