package edu.handong.csee.isel.data.collector.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.handong.csee.isel.data.collector.exception.FileFormatException;
import edu.handong.csee.isel.data.collector.io.CommitHashReader;
import edu.handong.csee.isel.data.collector.io.BFCWriter;
import edu.handong.csee.isel.data.collector.util.Utils;

/**
 * Extracts BFC and BIC from GitHub repository.
 * This class uses DPMiner and PySZZ.
 * All of the exe file path for these programs should be in the environment path.
 */
public class Extractor {
    private GitHubSearcher searcher;
    private String command;
    private String option;
    private String program;

    /**
     * Initializes the command and option for extracting BFC and BIC depends on the OS system.
     * Set searcher of this instance with the given searcher.
     * @param searcher the searcher
     */
    public Extractor(GitHubSearcher searcher) {
        boolean isWindows = System.getProperty("os.name")
                                  .toLowerCase()
                                  .startsWith("windows");
        
        this.searcher = searcher;

        if (isWindows) {
            command = "cmd.exe"; 
            option = "/c";
            program = "DPMiner.bat";
        } else {
            command = "sh";
            option = "-c";
            program = "DPMiner";
        }         
    }

    /**
     * Extracts BFC of this instance's repository in range of [<code>startDate</code>, <code>endDate</code>) and writes it in csv file using DPMiner.
     * The file is written in <code>projectPath</code>/out/bfc.
     * @param url the url of the respository 
	 * @param key the Jira key of the repository
     * @param startDate start date of the repositories' BFC
     * @param endDate end date of the repositories' BFC
     * @throws IOException
     * @throws InterruptedExcption
     * @throws FileFormatException
     */
    public void extractBFC(String url, String key, 
            			   String startDate, String endDate) 
                    				throws IOException, InterruptedException, 
                            		  	   FileFormatException {
		CommitHashReader reader;
        String DPMinerPath = String.join(File.separator, 
							  			"..", "tools", 
                                        "DPMiner", "bin", program);
        String patchPath = String.join(File.separator, "out", "patch");
        String argument = String.join(" ", 
                                      DPMinerPath, "patch", 
                                      "-i", url, "-o", patchPath, 
                                      "-ij", "-jk", key);
        String fileName = String.join(
                File.separator, 
                Utils.projectPath, "out", "bfc", 
				"bfc_" + searcher.getRepository() + ".json"); 
        BFCWriter writer = new BFCWriter(fileName);
	    
		Utils.execute(command, option, argument, Utils.projectPath);
	
		reader = new CommitHashReader(String.join(
                        File.separator, 
                        Utils.projectPath, patchPath, 
						"PATCH_" + searcher.getRepository() + ".csv"));
		
        writer.writeBFC(searcher.getRepouser(), searcher.getRepository(), 
						reader.readCommitHashes(searcher.getRepository(), 
												startDate, endDate));
        reader.close();
        writer.close();
    }

    /**
     * Extracts BIC of this instance's repository and writes it in JSON file by using PySZZ.
     * The file will be written in <code>projectPath</code>/out.
     * @throws IOException
     * @throws InterruptedException
     */
    public void extractBIC() 
            throws IOException, InterruptedException {
		String mainPath = String.join(File.separator, 
                                      "..", "tools", "pyszz", 
                                      "main.py");
        String BFCPath = String.join(
                File.separator, 
                "out", "bfc", 
                "bfc_" + searcher.getRepository() + ".json");
		String ymlPath = String.join(File.separator, 
                                     "..", "tools", "pyszz", 
                                     "conf", "raszz.yml");
        String repoPath = String.join(File.separator, "out", "snapshot");
        String argument = String.join(" ", 
                                      "python", mainPath, 
                                      BFCPath, ymlPath, repoPath);
		String outPath = String.join(File.separator, Utils.projectPath, "out");
        
        Utils.execute(command, option, argument, Utils.projectPath);    
        Files.move(Path.of(outPath, 
                           new File(outPath).list(
                                new FilenameFilter() {
                                    @Override 
                                    public boolean accept(File dir, 
                                                          String name) {
                                        return name.matches(
                                                "bic_ra_\\d+.json");  
                                    }
                                })[0]),  
				   Path.of(outPath, 
                          "bic", "bic_" + searcher.getRepository() + ".json"));
    }
}

