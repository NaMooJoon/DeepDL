package edu.handong.csee.isel.data.collector.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.handong.csee.isel.data.collector.exception.FileFormatException;
import edu.handong.csee.isel.data.collector.io.CommitHashReader;
import edu.handong.csee.isel.data.collector.io.PropertyWriter;
import edu.handong.csee.isel.data.collector.util.Resources;

/**
 * Extracts BFC and BIC from GitHub repository.
 * This class uses DPMiner and PySZZ.
 * All of the exe file path for these programs should be in the environment path.
 */
public class Extractor {
    private String fileSeparator = System.getProperty("file.separator");
    private String projectPath;
    
    /**
     * Sets this instance's project path.
     * @param projectPath the project path
     */
    public Extractor(String projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * Extracts BFC from [<code>startDate</code>, <code>endDate</code>) and writes it in csv file using DPMiner.
     * The file will be wrote in <code>projectPath</code>/out.
     * @param resources GitHub respositories informations
     * @param startDates start dates of the repositories' BFC
     * @param endDate end date of the repositories' BFC
     * @throws IOException
     * @throws InterruptedExcption
     * @throws FileFormatException
     */
    public void extractBFC(List<String>[] resources, 
            String[] startDates, String endDate) 
                    throws IOException, InterruptedException, 
                            FileFormatException {
        final String WINDOWS_FORMAT = 
                "cmd.exe /c %s.bat patch -i %s -o %s -ij -jk %s";
        final String LINUX_FORMAT = 
                "sh -c ./%s patch -i %s -o %s -ij -jk %s";
        
        String format = System.getProperty("os.name")
                              .toLowerCase()
                              .startsWith("windows") 
                                ? WINDOWS_FORMAT
                                : LINUX_FORMAT; 
        String DPMinerPath = String.join(fileSeparator, 
                                         "tool", "dpminer", "DPMiner");   
        String patchPath = String.join(fileSeparator, "out", "patches");
        List<String> urls = resources[Resources.URL.ordinal()];
        List<String> keys = resources[Resources.KEY.ordinal()];
        List<String> repousers = resources[Resources.REPOUSER.ordinal()];
        List<String> repositories = resources[Resources.REPOSITORY.ordinal()];
        String fileName = String.join(fileSeparator, patchPath, "out", "bfc.json"); 
        CommitHashReader reader = new CommitHashReader();
        PropertyWriter writer = new PropertyWriter(fileName);
        ArrayList<String>[] hashes = new ArrayList[urls.size()];
        
        for (int i = 0; i < urls.size(); i++) { 
            Object[] args = new Object[] { DPMinerPath, urls.get(i), 
                                           patchPath, keys.get(i) };                      

            execute(String.format(format, args), projectPath);
            reader.changeFile(
                        new File(patchPath, 
                                 "PATCH_" + repositories.get(i) + ".csv"));
            
            hashes[i] = reader.readCommitHashes(repositories.get(i), 
                                                startDates[i], endDate);
        }
        writer.writePySZZJson(repousers, repositories, hashes);
    }

    /**
     * Extracts BIC and writes it in json file using PySZZ.
     * The file will be wrote in <code>projectPath</code>/out.
     * @throws IOException
     * @throws InterruptedException
     */
    public void extractBIC() throws IOException, InterruptedException {
        final String WINDOWS_FORMAT = 
                "cmd.exe /c python %s %s tools\\pyszz\\conf\\raszz.yaml %s";
        final String LINUX_FORMAT = 
                "sh -c python %s %s tools/pyszz/conf/raszz.yaml %s";
        
        String format = System.getProperty("os.name")
                              .toLowerCase()
                              .startsWith("windows")
                                ? WINDOWS_FORMAT
                                : LINUX_FORMAT;
        String PySZZPath = String.join(fileSeparator, 
                                       "tools", "pyszz", "main.py");
        String BFCPath = String.join(fileSeparator, "out", "bfc.json");
        String repoPath = String.join(fileSeparator, "out", "repositories");
        
        execute(String.format(format, PySZZPath, BFCPath, repoPath), 
                projectPath);
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

