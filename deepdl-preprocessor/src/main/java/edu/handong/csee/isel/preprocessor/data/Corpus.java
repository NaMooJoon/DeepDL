package edu.handong.csee.isel.preprocessor.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


// import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.FIELDSET;

import me.tongfei.progressbar.*;

public class Corpus extends Converter {

    private String resourceAddr = "";

    public Corpus(String resourceAddr) {
        this.resourceAddr = resourceAddr;
    }

    public void make() throws FileNotFoundException, IOException {
        File sourceDir, targetDir;
        StringBuilder cleancode;
        BufferedWriter writer;

        /* initialize the file and close */
        new FileOutputStream(resourceAddr + "sentencepiece/corpus.txt", false).close(); 

        /* add lines into 'resource/sentencepiece/corpus.txt' for each files. */
        sourceDir = new File(resourceAddr + "train_resource/");
        System.out.println("[Generate Corpus text file]");
        System.out.println("Starting to make Corpus text file from the clean code!");

        for (String projectName : sourceDir.list()) {
            cleancode = new StringBuilder();
            targetDir = new File(resourceAddr + "train_resource/" + projectName);
            try (ProgressBar pb = new ProgressBar(String.format("%15s", projectName), targetDir.list().length)) {
                for (String fileName : targetDir.list()) {
                    append("train_resource/" + projectName + '/' + fileName, cleancode); /* add new file data into 'lineBlocks' */
                    pb.step();
                }
                pb.setExtraMessage("...Done!!");
            }
            File out = new File(resourceAddr + "sentencepiece/" + projectName + "_corpus.txt");
            out.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(out, false));
            writer.write(cleancode.toString());
            writer.close();
        }
    }
    private void append(String inputFileName, StringBuilder code) {
        File inputFile;
        BufferedReader br;

        if (!inputFileName.endsWith(".java")) 
            return ;
        try {
            String line;

            /* set the file stream to read */
            inputFile = new File(resourceAddr + inputFileName);
            br = new BufferedReader(new FileReader(inputFile));

            /* read the line in the file & store in 'code' */
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (isCommentLine(line)) {
                    continue;
                }
                code.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void make1() throws FileNotFoundException, IOException {

        File lineblocks = new File(resourceAddr + "lineblocks.json");
        BufferedReader br = new BufferedReader(new FileReader(lineblocks));
        StringBuilder cleancode = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("\"")) {
                line = line.substring(1, line.length());
            }
            if (line.endsWith(",")) {
                line = line.substring(0, line.length()-1);
            }
            if (line.endsWith("\"")) {
                line = line.substring(0, line.length()-1);
            }
            if (line.startsWith("[") || line.startsWith("]")) {
                continue;
            }
            cleancode.append(line + "\n");
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(resourceAddr + "sentencepiece/corpus.txt", false));
        writer.write(cleancode.toString());
        writer.close();
    }

    public void make2() throws FileNotFoundException, IOException {
        
        File sourceDir, projectDir;
        StringBuilder code;
        BufferedWriter writer;


        /* initialize the file and close */
        new FileOutputStream(resourceAddr + "sentencepiece/corpus.txt", false).close(); 

        /* add lineblocks into 'lineBlocks' for each files. */
        sourceDir = new File(resourceAddr + "apache/");
        code = new StringBuilder();
        System.out.println("[Generate the Corpus file]");
        System.out.println("Starting to make corpus file from the clean code!");
        for (String projectName : sourceDir.list()) {
            projectDir = new File(resourceAddr + "apache/" + projectName);
            try (ProgressBar pb = new ProgressBar(String.format("%15s", projectName), projectDir.list().length)) {
                for (String fileName : projectDir.list()) {
                    append("apache/" + projectName + '/' + fileName, code); /* add new file data into 'code' */
                    pb.step();
                }
                pb.setExtraMessage("...Done!!");
            }
        }

        /* write the file in txt form. */
        writer = new BufferedWriter(new FileWriter(resourceAddr + "sentencepiece/corpus.txt", true));
        writer.write(code.toString());
        writer.close();

    }

    private void append1(String inputFileName, StringBuilder code) {

        File inputFile;
        BufferedReader br;

        if (!inputFileName.endsWith(".java")) 
            return ;
        try {
            String line;

            /* set the file stream to read */
            inputFile = new File(resourceAddr + inputFileName);
            br = new BufferedReader(new FileReader(inputFile));

            /* read the line in the file & store in 'code' */
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (isCommentLine(line)) {
                    continue;
                }
                
                code.append(line + "\n");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
