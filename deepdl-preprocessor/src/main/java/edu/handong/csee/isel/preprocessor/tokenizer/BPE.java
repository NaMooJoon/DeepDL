package edu.handong.csee.isel.preprocessor.tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;

/**
 * Class that tokenizes the source code data for DeepDL model learning.
 * By excuting external program using python-BPE.
 * python-BPE: https://github.com/soaxelbrooke/python-bpe.git
 */
public class BPE extends Tokenizer {

    private String resourceAddr = "";

    public BPE(String resourceAddr) {
        this.resourceAddr = resourceAddr;
    }

    public void makeDictionary() {
        File sourceDir;

        sourceDir = new File(resourceAddr + "train_resource/");
        for (String projectName : sourceDir.list()) {

            if (!isFileExist(resourceAddr + "sentencepiece/" + projectName +"_corpus.txt")) {
                System.out.println("There is no 'corpus.txt' file..");
                return;
            }

            CommandLine cmdLine = new CommandLine("python3");
            cmdLine.addArgument(resourceAddr + "sentencepiece/modelTrain.py");
            cmdLine.addArgument(resourceAddr + "sentencepiece/");
            cmdLine.addArgument(projectName);

            DefaultExecutor executor = new DefaultExecutor();

            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

            ExecuteWatchdog watchdog = new ExecuteWatchdog(150000);
            executor.setWatchdog(watchdog);
            try { executor.execute(cmdLine, resultHandler); } 
            catch (IOException e) { e.printStackTrace(); }

        }
    }

    public void tokenize() {

        /* command line for a new process. */
        CommandLine cmdLine = new CommandLine("python3");
        cmdLine.addArgument(resourceAddr + "sentencepiece/tokenizer_train.py");
        cmdLine.addArgument(resourceAddr);

        DefaultExecutor executor = new DefaultExecutor();

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(50000);
        executor.setWatchdog(watchdog);
        try { executor.execute(cmdLine, resultHandler); } 
        catch (IOException e) { e.printStackTrace(); }

    }

    public void tokenize(String input, String output) {
        /* command line for a new process. */
        CommandLine cmdLine = new CommandLine("python3");
        cmdLine.addArgument(resourceAddr + "sentencepiece/tokenizer.py");
        // cmdLine.addArgument(resourceAddr + "sentencepiece"); // python execute location
        cmdLine.addArgument(resourceAddr + input);
        cmdLine.addArgument(resourceAddr + output);

        DefaultExecutor executor = new DefaultExecutor();
        
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        
        ExecuteWatchdog watchdog = new ExecuteWatchdog(50000);
        executor.setWatchdog(watchdog);
        try { executor.execute(cmdLine, resultHandler); } 
        catch (IOException e) { e.printStackTrace(); }
        
    }

    /* tokenize for the test file */
    public void tokenize(String input, String output, String project) {
        /* command line for a new process. */
        CommandLine cmdLine = new CommandLine("python3");
        cmdLine.addArgument(resourceAddr + "sentencepiece/tokenizer.py");
        // cmdLine.addArgument(resourceAddr + "sentencepiece"); // python execute location
        cmdLine.addArgument(resourceAddr + input);
        cmdLine.addArgument(resourceAddr + output);
        cmdLine.addArgument(project);

        DefaultExecutor executor = new DefaultExecutor();
        
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        
        ExecuteWatchdog watchdog = new ExecuteWatchdog(50000);
        executor.setWatchdog(watchdog);
        try { executor.execute(cmdLine, resultHandler); } 
        catch (IOException e) { e.printStackTrace(); }
        
    }

    boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    } 

}
