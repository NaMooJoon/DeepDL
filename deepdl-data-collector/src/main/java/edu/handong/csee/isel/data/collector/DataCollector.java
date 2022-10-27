package edu.handong.csee.isel.data.collector;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;

/**
 * Collector that collects data for DeepDL model.
 */
public class DataCollector {
    private String fileSeparator = System.getProperty("file.separator");
    
    /**
     * Collects data for DeepDL model.
     */
    public void collect() {
        
         


        
    }

    /**
     * Gets this project's absolute root path.
     * @return the project path
     */
    private String getProjectPath() {
        final int NUM_STEPS = 3; 
        
        String[] splittedCwd = System.getProperty("user.dir")
                                     .split(fileSeparator);        
        ArrayList<String> splittedDPMinerPath = new ArrayList<>();  
        
        for (int i = 0; i < splittedCwd.length - NUM_STEPS; i++) {
            splittedDPMinerPath.add(splittedCwd[i]);
        }  
        return String.join(fileSeparator, splittedDPMinerPath);  
    }

    /**
     * Loads resources to the given array.
     * Index 0 represents uris and index 1 represents jira keys.
     * @param resources resource array
     */
    private void loadResources(List<String>[] resources) {
        String projectPath = getProjectPath();
        Path resourcePath = Path.of(projectPath, "src", "main", "resources");

        try {
            resources[0] = Files.readAllLines(resourcePath.resolve("uri"));
            resources[1] = Files.readAllLines(resourcePath.resolve("jira-key"));
            
        } catch(IOException e) {

        } 
    }
}
