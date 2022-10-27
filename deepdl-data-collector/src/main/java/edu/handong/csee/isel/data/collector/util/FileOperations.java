package edu.handong.csee.isel.data.collector.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Class that provides useful file operations.
 */
public class FileOperations {
    
    /**
     * Copies <code>src</code> directory into <code>dst</code> directory with all of its entries.
     * @param dst destination path
     * @param src source path
     */
    public static void copyDirectory(Path dst, Path src) {
        try {
            Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                private int srcNameCount = src.getNameCount();

                @Override 
                public FileVisitResult preVisitDirectory(Path dir, 
                        BasicFileAttributes attrs) throws IOException {
                    Files.createDirectory(dst.resolve(dir.subpath(
                            srcNameCount - 1, dir.getNameCount())));
                  
                    return FileVisitResult.CONTINUE;    
                }

                @Override 
                public FileVisitResult visitFile(Path file, 
                        BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, dst.resolve(file.subpath(
                            srcNameCount - 1, file.getNameCount())));
                    
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unpacks all of the entries of the given directory.
     * @param dir the directory
     */
    public static void unpack(Path dir) {
     
    


    }

    public static void clean(Path dir, String remain) {

    }
}
