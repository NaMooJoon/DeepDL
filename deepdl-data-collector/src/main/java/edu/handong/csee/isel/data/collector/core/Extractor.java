package edu.handong.csee.isel.data.collector.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import edu.handong.csee.isel.data.collector.exception.FileFormatException;
import edu.handong.csee.isel.data.collector.io.CommitHashReader;
import edu.handong.csee.isel.data.collector.io.PropertyWriter;

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
     * @param urls GitHub repository urls
     * @param jiraKeys the Jira keys 
     * @param startDates start dates of the repositories' commits
     * @param endDate end date of the repositories' commits
     */
    public void extractBFC(String[] urls, String[] jiraKeys, 
                           String[] startDates, String endDate) {
        final String WINDOWS_FORMAT = 
                "cmd.exe /c %s.bat patch -i %s patch -o %s -ij -jk %s";
        final String LINUX_FORMAT = 
                "sh -c %s patch -i %s -o %s -ij -jk %s";
        final int REPOUSER = 1;
        final int REPOSITORY = 2;
        
        String format = System.getProperty("os.name")
                              .toLowerCase()
                              .startsWith("windows") 
                                ? WINDOWS_FORMAT
                                : LINUX_FORMAT; 
        String fileName = String.join(fileSeparator, "out", "bfc.json"); 
        String DPMinerPath = String.join(fileSeparator, 
                                         "tools", "DPMiner", "DPMiner");   
        String patchPath = String.join(fileSeparator, "out", "patch");
        CommitHashReader reader = new CommitHashReader();
        String[] repousers = new String[urls.length];
        String[] repositories = new String[urls.length];
        ArrayList<String>[] hashes = new ArrayList[urls.length];
        File[] patches;

        try {
            PropertyWriter writer = new PropertyWriter(fileName);

            for (int i = 0; i < urls.length; i++) { 
                Object[] args = new Object[] { DPMinerPath, urls[i], 
                                               patchPath, jiraKeys[i] };
                String[] splittedUrl;                        

                execute(String.format(format, args), projectPath);

                splittedUrl = urls[i].split("/"); 
                repousers[i] = splittedUrl[REPOUSER];
                repositories[i] = splittedUrl[REPOSITORY];
            }
            patches = new File(patchPath).listFiles(new FileFilter() {
                
                @Override
                public boolean accept(File file) {
                    return file.getName().matches("PATCH_\\w+.csv");
                }
            });

            Arrays.sort(patches, new Comparator<File>() {

                @Override
                public int compare(File o1, File o2) {
                    return (int) (o1.lastModified() - o2.lastModified());
                }    
            });

            for (int i = 0; i < urls.length; i++) {
                reader.changeFile(patches[i]);
                
                hashes[i] = reader.readCommitHashes(repositories[i], 
                                                    startDates[i], endDate);
            }
            writer.writePySZZJson(repousers, repositories, hashes);
        } catch(IOException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        } catch(FileFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts BIC and writes it in json file using PySZZ.
     * The file will be wrote in <code>projectPath</code>/out.
     */
    public void extractBIC() {
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
        
        try {
            execute(String.format(format, PySZZPath, BFCPath, repoPath), 
                    projectPath);
        } catch(IOException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
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
