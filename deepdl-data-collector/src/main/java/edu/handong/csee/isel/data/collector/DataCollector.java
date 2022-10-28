package edu.handong.csee.isel.data.collector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import edu.handong.csee.isel.data.collector.core.GitHubSearcher;

/**
 * Collector that collects data for DeepDL model.
 */
public class DataCollector {
    private String fileSeparator = System.getProperty("file.separator");
    private String projectPath = getProjectPath();
    
    /**
     * Collects data for DeepDL model.
     */
    public void collect() {
        String repoPath = String.join(fileSeparator, 
                                      projectPath, "out", "repositories");
        String snapshotPath = String.join(fileSeparator,  
                                          projectPath, "out", "snapshots");
        CloneCommand cloneCommand = new CloneCommand();                                  
        List<String>[] resources = new List[4];
        
        loadResources(resources);
        
        for (int i = 0; i < resources[0].size(); i++) {
            
        }
      
        
        
    }

    /**
     * Gets this project's absolute root path.
     * @return the project path
     */
    private String getProjectPath() {
        final int NUM_STEPS = 3; 
        
        String[] splittedCwd = System.getProperty("user.dir")
                                     .split(fileSeparator);        
        String[] splittedProjectPath = new String[splittedCwd.length - NUM_STEPS];  
        
        for (int i = 0; i < splittedProjectPath.length; i++) {
            splittedProjectPath[i] = splittedCwd[i];
        }  
        return String.join(fileSeparator, splittedProjectPath);  
    }

    /**
     * Loads resources to the given array.
     * Index 0: represents uris
     * Index 1: represents jira keys.
     * Index 2: represents repousers
     * Index 3: represents repositories
     * @param resources resource array
     */
    private void loadResources(List<String>[] resources) {
        Path resourcePath = Path.of(projectPath, "src", "main", "resources");

        try {
            resources[0] = Files.readAllLines(resourcePath.resolve("uri"));
            resources[1] = Files.readAllLines(resourcePath.resolve("jira-key"));
            
            for (int i = 2; i <= 3; i++) {
                resources[i] = new ArrayList<String>();
            }

            for (String uri : resources[0]) {
                String[] splitted = uri.split("/");
                
                for (int i = 2; i <= 3; i++) {
                    resources[i].add(splitted[i - 1]);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } 
    }

    /**
     * Clones given repository into <code>projectPath</code>/out/repositories and <code>projectPaht</code>/out/snapshots.
     * @param uri GitHub respository uri
     * @param repuserPath path to the repository repouser
     * @param snapshotPath path to the snapshots
     */
    private void cloneRepository(String uri, 
            String repouserPath, String snapshotPath) {
        File repouser = new File(repouserPath);
        CloneCommand cloneCommand = new CloneCommand();

        if (!repouser.exists()) {
            repouser.mkdir();
        }
        try {
            cloneCommand.setURI(uri).setDirectory(repouser).call().close();
            cloneCommand.setDirectory(new File(snapshotPath)).call().close();
        } catch (InvalidRemoteException e) {
            e.printStackTrace();
        } catch (TransportException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }
}
