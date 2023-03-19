package edu.handong.csee.isel.data.collector.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
    private String command;
    private String option;
    private String program;
    private String projectPath = Utils.getProjectPath();
    
    /**
     * Initializes the command and option for extracting BFC and BIC depends on the OS system.
     */
    public Extractor() {
        boolean isWindows = System.getProperty("os.name")
                                  .toLowerCase()
                                  .startsWith("windows");
        
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
     * Extracts BFC of the given GitHub repository from [<code>startDate</code>, <code>endDate</code>) and writes it in csv file using DPMiner.
     * The file will be written in <code>projectPath</code>/out/bfc.
     * @param url the url of the respository 
	 * @param key the Jira key of the repository
	 * @param repouser the repouser of the repository
	 * @param repository the name of the repository
     * @param startDate start date of the repositories' BFC
     * @param endDate end date of the repositories' BFC
     * @throws IOException
     * @throws InterruptedExcption
     * @throws FileFormatException
     */
    public void extractBFC(String url, String key, 
						   String repouser, String repository, 
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
        String fileName = String.join(File.separator, 
                                      projectPath, "out", "bfc", 
									  "bfc_" + repository + ".json"); 
        BFCWriter writer = new BFCWriter(fileName);
	    
		execute(command, option, argument, projectPath);
	
		reader = new CommitHashReader(String.join(
                        File.separator, 
                        projectPath, patchPath, 
						"PATCH_" + repository + ".csv"));
		
        writer.writeBFC(repouser, repository, 
						reader.readCommitHashes(repository, 
												startDate, endDate));
        reader.close();
        writer.close();
    }

    /**
     * Extracts BIC of the given repository and writes it in JSON file by using PySZZ.
     * The file will be written in <code>projectPath</code>/out.
     * @param reponame the name of the repository
     * @throws IOException
     * @throws InterruptedException
     */
    public void extractBIC(String reponame) throws IOException, InterruptedException {
		String mainPath = String.join(File.separator, 
                                      "..", "tools", "pyszz", 
                                      "main.py");
        String BFCPath = String.join(File.separator, 
                                     "out", "bfc", 
                                     "bfc_" + reponame + ".json");
		String ymlPath = String.join(File.separator, 
                                     "..", "tools", "pyszz", 
                                     "conf", "raszz.yml");
        String repoPath = String.join(File.separator, "out", "snapshot");
        String argument = String.join(" ", 
                                      "python3", mainPath, 
                                      BFCPath, ymlPath, repoPath);
		String outPath = String.join(File.separator, projectPath, "out");
        
        execute(command, option, argument, projectPath);    
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
				   Path.of(outPath, "bic", "bic_" + reponame + ".json"));
    }

    /**
     * Executes the given command with the given option and argument in a subprocess.
     * @param command the command
     * @param option the option
     * @param argument the argument
     * @param dir the directory in which executes the command
     * @throws IOException
     * @throws InterruptedException
     */
    private void execute(String command, String option, String argument, 
                        String dir) 
                                throws IOException, InterruptedException {
        Process child = Runtime.getRuntime()
                               .exec(new String[] {command, option, argument}, 
                                     null, 
                                     new File(dir));
        
        flushWaitFor(child);
    }    

    /**
     * Flushes the given process' input stream and error stream.<p>
     * If the process has not yet terminated, the calling thread will be blocked until the process exits.
     * @param p the process 
     * @return exit value of the process
     */
    private int flushWaitFor(Process p) throws IOException {
        InputStream is = p.getInputStream();
        InputStream es = p.getErrorStream();
        
        while (p.isAlive()) {
            if (is.available() > 0) {
                System.out.print(new String(is.readNBytes(is.available())));
            }

            if (es.available() > 0) {
                System.out.print(new String(es.readNBytes(es.available())));
            }
        }
        
        return p.exitValue();
    }
}

