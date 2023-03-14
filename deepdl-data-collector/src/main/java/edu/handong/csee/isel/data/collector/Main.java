package edu.handong.csee.isel.data.collector;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.handong.csee.isel.data.collector.util.Utils;

public class Main {
    public static void main(String[] args) {    
        //new DataCollector().collect();
        try {
            String reponame = "hadoop";
            String mainPath = String.join(File.separator, 
                                          "..", "tools", "pyszz", 
                                          "main.py");
            String BFCPath = String.join(File.separator, 
                                         "out", "bfc", 
                                         "bfc_" + reponame + "copy.json");
            String ymlPath = String.join(File.separator, 
                                         "..", "tools", "pyszz", 
                                         "conf", "raszz.yml");
            String repoPath = String.join(File.separator, "out", "snapshot");
            String argument = String.join(" ", 
                                          "python", mainPath, 
                                          BFCPath, ymlPath, repoPath);
            String outPath = String.join(File.separator, 
                                         Utils.getProjectPath(), "out");

            Process child = 
                    Runtime.getRuntime().exec(
                            new String[] {"cmd.exe", "/c", argument}, 
                            null, 
                            new File(Utils.getProjectPath()));                    
            child.waitFor();
            /** 
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
            **/
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /** 
        try {
            String DPMinerPath = String.join(File.separator, 
                                             "..", "tools", 
                                             "DPMiner", "bin", "DPMiner.bat");
            String url = "https://github.com/apache/juddi";
            String patchPath = String.join(File.separator, 
                                           "out", "patch");
            String key = "JUDDI";
            String argument = String.join(" ", 
                                          DPMinerPath, "patch", 
                                          "-i", url, "-o", patchPath, 
                                          "-ij", "-jk", key);
            Process child = 
                    Runtime.getRuntime().exec(
                            new String[] {"cmd.exe", "/c", argument}, 
                            null, 
                            new File(Utils.getProjectPath()));
            
            child.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        **/
    }
}
