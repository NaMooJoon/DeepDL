package edu.handong.csee.isel.data.collector.util;

import java.util.ArrayList;

public class Utils {
    /**
     * Gets this project's absolute root directory path.
     * @return the project path
     */
    public static String getProjectPath() {
        final String PROJECT_DIR = "deepdl-data-collector";

        String fileSeparator = System.getProperty("file.separator");
        String regex = fileSeparator.equals("\\") 
                ? fileSeparator + fileSeparator 
                : fileSeparator;
        String[] splittedCwd = System.getProperty("user.dir").split(regex);        
        ArrayList<String> splittedProjectPath = new ArrayList<>();  
        
        for (String file : splittedCwd) {
            splittedProjectPath.add(file);

            if (file.equals(PROJECT_DIR)) {
                break;
            }
        }  
        return String.join(fileSeparator, splittedProjectPath); 
    }
    
}
