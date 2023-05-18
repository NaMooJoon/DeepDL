package edu.handong.csee.isel.data.collector.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class Utils {
    public static String projectPath = getProjectpath();

    /**
     * Sets this project's absolute project path.
     */
    public static String getProjectpath() {
        String regex = File.separator.equals("\\") ? "\\\\" : File.separator;
        String[] cwdPathElements = System.getProperty("user.dir").split(regex);        
        ArrayList<String> projectPathElements = new ArrayList<>();  

        for (String pathElement : cwdPathElements) {
            if (pathElement.equals("src")) {
                break;
            }

            projectPathElements.add(pathElement);
        }

        return String.join(File.separator, projectPathElements); 
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

    /**
     * Executes the given command with the given option and argument in a subprocess.
     * @param command the command
     * @param option the option
     * @param argument the argument
     * @param dir the directory in which executes the command
     * @throws IOException
     * @throws InterruptedException
     */
    public static void execute(String command, String option, String argument, 
                               String dir) throws IOException, 
                                                  InterruptedException {
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
    public static int flushWaitFor(Process p) throws IOException {
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
