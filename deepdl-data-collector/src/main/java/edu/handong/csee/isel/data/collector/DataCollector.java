package edu.handong.csee.isel.data.collector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.data.collector.core.DatasetMaker;
import edu.handong.csee.isel.data.collector.core.Extractor;
import edu.handong.csee.isel.data.collector.core.GitHubSearcher;
import edu.handong.csee.isel.data.collector.util.Resources;
import edu.handong.csee.isel.data.collector.util.Utils;

/**
 * Collector that collects data for DeepDL model.
 */
public class DataCollector {
    private String fileSeparator = System.getProperty("file.separator");
    private String projectPath = Utils.getProjectPath();
    
    /**
     * Collects data for DeepDL model.
     */
    public void collect() {
        final String END_DATE = "2021-11-30";
        final float TRAIN_RATIO = 0.6F;
        
        try (GitHubSearcher searcher = new GitHubSearcher()) {
            int numRepositories;    
            List<String>[] resources = new List[4];
            Extractor extractor = new Extractor();
            DatasetMaker maker = new DatasetMaker(searcher);

            loadResources(resources);

            numRepositories = resources[Resources.URL.ordinal()].size();

            Files.createDirectories(Path.of(projectPath, "out", "bfc"));
            Files.createDirectories(Path.of(projectPath, "out", "bic"));
            
            for (int i = 0; i < numRepositories; i++) {
                RevCommit splittingCommit;
                String repoPath = 
                        String.join(fileSeparator, 
                                projectPath, "out", "snapshot", 
                                resources[Resources.REPOUSER.ordinal()].get(i),
                                resources[Resources.REPOSITORY.ordinal()]
                                .get(i));
                
                if (!Files.exists(Path.of(repoPath))) {
                    searcher.cloneRepository(
                            resources[Resources.URL.ordinal()].get(i), 
                            repoPath);
                }

                searcher.changeRepository(
                        String.join(fileSeparator, repoPath, ".git"));
            
                splittingCommit = searcher.getSplittingCommit(TRAIN_RATIO, 
                                                              null, END_DATE);
          
                extractor.extractBFC(resources[Resources.URL.ordinal()].get(i), 
                        resources[Resources.KEY.ordinal()].get(i),
                        resources[Resources.REPOUSER.ordinal()].get(i), 
                        resources[Resources.REPOSITORY.ordinal()].get(i),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                Date.from(splittingCommit.getAuthorIdent()
                                                         .getWhen()
                                                         .toInstant()
                                                         .plusSeconds(1L))),
                        END_DATE);
                extractor.extractBIC(
                        resources[Resources.REPOSITORY.ordinal()].get(i));                        
                searcher.checkoutToSnapshot(splittingCommit);
                maker.makeDataset(
                        resources[Resources.REPOSITORY.ordinal()].get(i), 
                        splittingCommit.getAuthorIdent().getWhen());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads resources to the given array.<p>
     * Index 0: represents urls<p>
     * Index 1: represents jira keys<p>
     * Index 2: represents repousers<p>
     * Index 3: represents repositories<p>
     * @param resources resource array
     */
    private void loadResources(List<String>[] resources) {
        Path resourcePath = Path.of(projectPath, "src", "main", "resources");

        try {
            resources[Resources.URL.ordinal()] 
                    = Files.readAllLines(resourcePath.resolve("url"));
            resources[Resources.KEY.ordinal()] 
                    = Files.readAllLines(resourcePath.resolve("jira-key"));
            
            for (int i = 2; i <= 3; i++) {
                resources[i] = new ArrayList<String>();
            }

            for (String url : resources[Resources.URL.ordinal()]) {
                String[] splitted = url.split("/");
                
                for (int i = 2; i <= 3; i++) {
                    resources[i].add(splitted[i + 1]);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } 
    }
}
