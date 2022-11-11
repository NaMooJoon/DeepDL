package edu.handong.csee.isel.data.collector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.data.collector.core.Extractor;
import edu.handong.csee.isel.data.collector.core.GitHubSearcher;
import edu.handong.csee.isel.data.collector.util.Resources;

/**
 * Collector that collects data for DeepDL model.
 */
public class DataCollector {
    private String projectPath = getProjectPath();
    private String fileSeparator = System.getProperty("file.separator");
    private GitHubSearcher searcher = new GitHubSearcher();
    private Extractor extractor = new Extractor(projectPath);

    /**
     * Collects data for DeepDL model.
     */
    public void collect() {
        final String END_DATE = "2021-11-30";
        final float TRAIN_RATIO = 0.6F;

        String repoPath = String.join(fileSeparator, 
                                      projectPath, "out", "repositories");
        String snapshotPath = String.join(fileSeparator,  
                                          projectPath, "out", "snapshots");   
        List<String>[] resources = new List[4];
        RevCommit[] splittingCommits;
        String[] startDates;
        int numRepositories;

        loadResources(resources);

        numRepositories = resources[Resources.URL.ordinal()].size();
        splittingCommits = new RevCommit[numRepositories];
        startDates = new String[numRepositories];

        try {
            for (int i = 0; i < numRepositories; i++) {
                searcher.cloneRepository(
                        resources[Resources.URL.ordinal()].get(i), 
                        String.join(fileSeparator, repoPath, 
                                resources[Resources.REPOUSER.ordinal()]
                                .get(i)));
                searcher.cloneRepository(
                        resources[Resources.URL.ordinal()].get(i), 
                        snapshotPath);
                searcher.changeRepository(
                        String.join(fileSeparator, snapshotPath, 
                                resources[Resources.REPOSITORY.ordinal()]
                                .get(i), ".git"));

                splittingCommits[i] = 
                        searcher.getSplittingCommit(TRAIN_RATIO, 
                                                    null, END_DATE);
                startDates[i] = 
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                Date.from(splittingCommits[i]
                                          .getAuthorIdent()
                                          .getWhen()
                                          .toInstant()
                                          .plusSeconds(1L)));
            }
            extractor.extractBFC(resources, startDates, END_DATE);
            extractor.extractBIC();   
        } catch(Exception e) {
            e.printStackTrace();
        }

        /*TO DO: */
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
     * Index 0: represents urls
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

            for (String url : resources[0]) {
                String[] splitted = url.split("/");
                
                for (int i = 2; i <= 3; i++) {
                    resources[i].add(splitted[i - 1]);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } 
    }
}
