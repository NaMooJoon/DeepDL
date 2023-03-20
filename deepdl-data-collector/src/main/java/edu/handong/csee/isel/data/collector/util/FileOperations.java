package edu.handong.csee.isel.data.collector.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Class that provides useful file operations.
 */
public class FileOperations {
    
    /**
     * Unpacks all of the given extension files and removes the other files of the given directory.
     * @param dir the directory
     * @param extension the extension
     * @throws IOException
     */
    public static void unpack(Path dir, String extension) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            
            @Override 
            public FileVisitResult visitFile(Path file, 
                                             BasicFileAttributes attrs) 
                                                    throws IOException {
                if (file.getFileName().toString().endsWith("." + extension)) {
                    Files.move(file, dir.resolve(file.getFileName()), 
                               StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.delete(file);
                }
                
                return FileVisitResult.CONTINUE;
            }
                                                     
            @Override 
            public FileVisitResult postVisitDirectory(Path dir, 
                    IOException exc) throws IOException {
                Files.delete(dir);
                
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
