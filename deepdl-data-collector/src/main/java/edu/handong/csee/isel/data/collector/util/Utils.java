package edu.handong.csee.isel.data.collector.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class Utils {
    public static final String PROJECT_DIR = "deepdl-data-collector";

    /**
     * Gets this project's absolute root directory path.
     * @return the project path
     */
    public static String getProjectPath() {
        String regex = File.separator.equals("\\") ? "\\\\" : File.separator;
        String[] splittedCwd = System.getProperty("user.dir").split(regex);        
        ArrayList<String> splittedProjectPath = new ArrayList<>();  
        
        for (String file : splittedCwd) {
            splittedProjectPath.add(file);

            if (file.equals(PROJECT_DIR)) {
                break;
            }
        }

        return String.join(File.separator, splittedProjectPath); 
    }

    /**
     * Unpacks all of the given extension files and removes the other files of the given directory.
     * @param rootDir the directory
     * @param extension the extension
     * @throws IOException
     */
    public static void unpack(Path rootDir, String extension) throws IOException {
    
        Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
            
            @Override 
            public FileVisitResult visitFile(Path file, 
                                             BasicFileAttributes attrs) 
                                                    throws IOException {
                if (file.getFileName().toString().endsWith("." + extension)) {
                    Files.move(file, rootDir.resolve(file.getFileName()), 
                               StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.delete(file);
                }
                
                return FileVisitResult.CONTINUE;
            }
                                                     
            @Override 
            public FileVisitResult postVisitDirectory(Path dir, 
                    IOException exc) throws IOException {
                if (!dir.equals(rootDir)) {
                    Files.delete(dir);
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
}
