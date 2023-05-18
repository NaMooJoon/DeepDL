package edu.handong.csee.isel.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;

import edu.handong.csee.isel.data.collector.DataCollector;
import edu.handong.csee.isel.data.collector.core.DatasetMaker;
import edu.handong.csee.isel.data.collector.core.GitHubSearcher;
import edu.handong.csee.isel.data.collector.exception.FileFormatException;
import edu.handong.csee.isel.data.collector.util.Utils;

class DeepDL {
    private String command;
    private String option;
    private String repository;
    private String repouser;

    public DeepDL() {
        if (System.getProperty("os.name")
                  .toLowerCase()
                  .startsWith("windows")) {
            command = "cmd.exe"; 
            option = "/c";
        } else {
            command = "sh";
            option = "-c";
        }
    }

    /**
     * Collects data for DeepDL model with the given GitHub url and Jira key.
     * @param url the GitHub url
     * @param key the Jira key
     * @throws IOExcpetion
     * @throws GitAPIException
     * @throws InterruptedException
     * @throws FileFormatException
     */
    public void collect(String url, String key) 
            throws IOException, GitAPIException, 
                   InterruptedException, FileFormatException {
        new DataCollector().collect(url, key);
    }

    public void preprocess() {

    }

    /**
     * Trains DeepDL model of the given weight file with the given data file.
     * @param weightFilename the absolute weight file name
     * @param dataFilename the absolute data file name
     * @throws IOException
     * @throws InterruptedException
     */
    public void train(String weightFilename, String dataFilename) 
            throws  IOException, InterruptedException {
        String[] pathElements = weightFilename.split(File.separator);
        String repouser = pathElements[pathElements.length - 3];
        String repository = pathElements[pathElements.length - 2];
        String outputPath = String.join(File.separator, 
                                        Utils.projectPath, 
                                        "out", "weights", 
                                        repouser, repository);
        Files.createDirectories(Path.of(outputPath));        
        Utils.execute(command, option, 
                      String.join(" ", 
                                  "python3.8 main.py 11723 -tr", 
                                  weightFilename, dataFilename, outputPath),
                      String.join(File.separator, 
                                  Utils.projectPath, 
                                  "..", "deepdl-model", "src"));
    }

    /**
     * Tests DeepDL model of the given weight file with the given data file.
     * @param weightFilename the absolute weight file name
     * @param dataFilename the absolute data file name
     * @throws IOException
     * @throws InterruptedException
     */
    public void test(String weightFilename, String dataFilename) 
            throws IOException, InterruptedException {
        String[] pathElements = weightFilename.split(File.separator);
        String repouser = pathElements[pathElements.length - 3];
        String repository = pathElements[pathElements.length - 2];
        String outputPath = String.join(File.separator, 
                                        Utils.projectPath, 
                                        "out", "plots", 
                                        repouser, repository);
        Files.createDirectories(Path.of(outputPath));   
        Utils.execute(command, option, 
                      String.join(" ", 
                                  "python3.8 main.py 11723 -ts", 
                                  weightFilename, dataFilename, outputPath),
                      String.join(File.separator, 
                                  Utils.projectPath, 
                                  "..", "deepdl-model", "src"));
    }

    /**
     * Runs DeepDL model of the given weight file  
     * and lists added lines of the given GitHub url's given commit.
     * The added lines are sorted by line entropy in descending order.
     * @param url the GitHub url
     * @param hash the commit hash
     * @param weightFilename the DeepDL model weight file name
     * @throws GitAPIException
     * @throws IOException
     * @throws InterruptedException
     */
    public void run(String url, String hash, String weightFilename) 
            throws GitAPIException, IOException, InterruptedException {
        String[] urlElements = url.split("/");
        String repouser = urlElements[urlElements.length - 2];
        String repository = urlElements[urlElements.length - 1];
        String repoPath = String.join(File.separator,
                                      Utils.projectPath, 
                                      "out", "snapshot", 
                                      repouser, repository);
        String dataPath = String.join(File.separator,
                                      Utils.projectPath,
                                      "out", "test-data",
                                      repouser, repository);
        String rankingPath = String.join(File.separator, 
                                         Utils.projectPath,
                                         "out", "ranking",
                                         repouser, repository);
        String shortHash = hash.substring(0, 5);
        String rawDataFilename = "raw_" + shortHash + ".csv";
        String preprocessedDataFilename = "processed_" + shortHash + ".csv";
        String rawRankingFilename = "raw_ranking_" + shortHash + ".txt";
        String rankingFilename = "ranking_" + shortHash + ".txt";
        GitHubSearcher searcher = new GitHubSearcher();
        DatasetMaker maker = new DatasetMaker(searcher);
        
        Files.createDirectories(Path.of(dataPath)); 
        Files.createDirectories(Path.of(rankingPath)); 
    
        if (!Files.exists(Path.of(repoPath))) {
            searcher.cloneRepository(url, repoPath);
        }
        
        searcher.changeRepository(String.join(repoPath, ".git"));
        maker.saveBICWithPinpointedBuggyLines(hash, 
                maker.makeRecords(
                        maker.getAddedAndMaintainedLines(hash, "java"), 
                        new HashMap<>()));
        Utils.execute(command, option, 
                      String.join(" ", 
                                  "python3.8 tokenizer.py",
                                  dataPath + File.separator + rawDataFilename,  
                                  dataPath + File.separator 
                                  + preprocessedDataFilename,  
                                  repository),
                      String.join(File.separator, 
                                  Utils.projectPath, 
                                  "..", "deepdl-preprocessor", 
                                  "src", "main", "resources", "python"));
        Utils.execute(command, option, 
                      String.join(" ", 
                                  "python3.8 main.py 11723 -a",
                                  weightFilename,  
                                  dataPath + File.separator 
                                  + preprocessedDataFilename,
                                  rankingPath + File.separator 
                                  + rawRankingFilename),
                      String.join(File.separator, 
                                  Utils.projectPath, 
                                  "..", "deepdl-model", "src"));
        // execute tokenizer
        Utils.execute(command, option, 
                      String.join(" ", 
                                  "python3.8 main.py 11723 -a",
                                  weightFilename,  
                                  dataPath + File.separator 
                                  + preprocessedDataFilename,
                                  rankingPath + File.separator 
                                  + rawRankingFilename),
                      String.join(File.separator, 
                                  Utils.projectPath, 
                                  "..", "deepdl-model", "src"));
        /** 
        try (BufferedReader reader = new BufferedReader(new FileReader(
                    new File(String.join(File.separator, 
                                         rankingPath, rankingFilename))))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        **/
    }
}
