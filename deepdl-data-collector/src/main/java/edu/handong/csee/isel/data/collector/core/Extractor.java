package edu.handong.csee.isel.data.collector.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import edu.handong.csee.isel.data.collector.exception.FileFormatException;
import edu.handong.csee.isel.data.collector.io.CommitHashReader;
import edu.handong.csee.isel.data.collector.io.BFCWriter;
import edu.handong.csee.isel.data.collector.util.Resources;
import edu.handong.csee.isel.data.collector.util.Utils;

/**
 * Extracts BFC and BIC from GitHub repository.
 * This class uses DPMiner and PySZZ.
 * All of the exe file path for these programs should be in the environment path.
 */
public class Extractor {
    private String fileSeparator = System.getProperty("file.separator");
    private String projectPath = Utils.getProjectPath();
    private boolean isWindows = System.getProperty("os.name")
                                      .toLowerCase()
                                      .startsWith("windows");

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
        final String WINDOWS_FORMAT = 
                "cmd.exe /c %s patch -i %s -o %s -ij -jk %s";
        final String LINUX_FORMAT = 
                "sh -c %s patch -i %s -o %s -ij -jk %s";
        
		CommitHashReader reader;
        String format = isWindows ? WINDOWS_FORMAT : LINUX_FORMAT; 
        String DPMinerPath = 
				isWindows ? String.join(fileSeparator, 
							  			"..", "tools", "dpminer", 
										"bin", "DPMiner.bat")
                		  : String.join(fileSeparator, 
							  			"..", "tools", "dpminer", 
										"bin", "DPMiner");
        String patchPath = String.join(fileSeparator, "out", "patch");
        String fileName = String.join(fileSeparator, 
                                      projectPath, "out", "bfc", 
									  "bfc_" + repository + ".json"); 
		Object[] args = new Object[] { DPMinerPath, url, 
									   patchPath, key };  
        BFCWriter writer = new BFCWriter(fileName);
	                    
		execute(String.format(format, args), projectPath);
		
		reader = 
				new CommitHashReader(
						new File(String.join(fileSeparator, 
								 			 projectPath, patchPath), 
								 "PATCH_" + repository + ".csv"));
		
        writer.writeBFC(repouser, repository, 
						reader.readCommitHashes(repository, 
												startDate, endDate));
        reader.close();
        writer.close();
    }

    /**
     * Extracts BIC and writes it in json file using PySZZ.
     * The file will be written in <code>projectPath</code>/out.
     * @throws IOException
     * @throws InterruptedException
     */
    public void extractBIC(String repository) throws IOException, InterruptedException {
        final String WINDOWS_FORMAT = 
                "cmd.exe /c python %s %s tools\\pyszz\\conf\\raszz.yml %s";
        final String LINUX_FORMAT = 
                "sh -c python %s %s tools/pyszz/conf/raszz.yaml %s";
        
        String format = isWindows ? WINDOWS_FORMAT : LINUX_FORMAT;
        String PySZZPath = String.join(fileSeparator, 
                                       "..", "tools", "pyszz");
		String mainPath = String.join(fileSeparator, PySZZPath, "main.py");
        String BFCPath = String.join(fileSeparator, "out", "bfc", "bfc_" + repository + ".json");
		String ymlPath = String.join(fileSeparator, PySZZPath, "conf", "raszz.yml");
        String repoPath = String.join(fileSeparator, "out", "snapshot");
		String PySZZOutPath = String.join(fileSeparator, projectPath, PySZZPath, "out");

        execute(String.format(format, mainPath, BFCPath, ymlPath, repoPath), 
                projectPath);    
        Files.move(Path.of(PySZZOutPath, new File(PySZZOutPath).list()[0]),
				   Path.of(projectPath, "out", "bic", "bic_" + repository + ".json"));
    }

    /**
     * Executes either windows or linux commad based on the user's os system.
     * @param command a command to execute
     * @param dir the directory in which executes the command
     * @throws IOException
     * @throws InterruptedException
     */
    private void execute(String command, String dir) 
            throws IOException, InterruptedException {
        Process child = Runtime.getRuntime()
                               .exec(command, null, new File(dir));

        child.waitFor();
    }    
}

