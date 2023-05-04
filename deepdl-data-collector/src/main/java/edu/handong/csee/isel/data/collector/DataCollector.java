package edu.handong.csee.isel.data.collector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.data.collector.core.DatasetMaker;
import edu.handong.csee.isel.data.collector.core.Extractor;
import edu.handong.csee.isel.data.collector.core.GitHubSearcher;
import edu.handong.csee.isel.data.collector.exception.FileFormatException;
import edu.handong.csee.isel.data.collector.util.Utils;

/**
 * Collector that collects data for DeepDL model.
 */
public class DataCollector {
    private GitHubSearcher searcher = new GitHubSearcher();
    private Extractor extractor = new Extractor(searcher);
    private DatasetMaker maker = new DatasetMaker(searcher);
    private String projectPath = Utils.getProjectPath();
    
    /**
     * Collects data from the repository of the given url and key for DeepDL model.
     * @param url the github repository url
     * @param key the jira key
     */
    public void collect(String url, String key) 
            throws IOException, GitAPIException, 
                   InterruptedException, FileFormatException {
        final String END_DATE = "2021-11-30";
        final float TRAIN_RATIO = 0.6F;

        RevCommit splittingCommit;
        String[] elements = url.split("/");
        String repouser = elements[elements.length - 2];
        String repository = elements[elements.length - 1];
        String repoPath = String.join(File.separator, 
                                      projectPath, "out", "snapshot", 
                                      repouser, repository);

        searcher.setRepouser(repouser);
        searcher.setRepository(repository);
        Files.createDirectories(Path.of(projectPath, "out", "bfc"));
        Files.createDirectories(Path.of(projectPath, "out", "bic"));
        Files.createDirectories(Path.of(projectPath, 
                                        "out", "test-data", 
                                        repouser, repository));
        
        if (!Files.exists(Path.of(repoPath))) {
            searcher.cloneRepository(url, repoPath);
        }

        searcher.changeRepository(String.join(File.separator, 
                                              repoPath, ".git"));
    
        splittingCommit = searcher.getSplittingCommit(TRAIN_RATIO, 
                                                      null, END_DATE);
        
        extractor.extractBFC(
                url, key, 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                        Date.from(splittingCommit.getAuthorIdent()
                                                 .getWhen()
                                                 .toInstant()
                                                 .plusSeconds(1L))),
                END_DATE);
        extractor.extractBIC();                   
        searcher.checkoutToSnapshot(splittingCommit);
        maker.makeDataset(splittingCommit.getAuthorIdent().getWhen());
        Utils.unpack(Path.of(repoPath), "java");
    }

    /**
     * Loads resources to the given array.<p>
     * Index 0: represents urls<p>
     * Index 1: represents jira keys<p>
     * Index 2: represents repousers<p>
     * Index 3: represents repositories<p>
     * @param resources resource array
     */
    /** 
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
    **/
}
