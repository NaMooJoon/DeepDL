package edu.handong.csee.isel.preprocessor.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.csv.*;

import me.tongfei.progressbar.*;
import edu.handong.csee.isel.preprocessor.tokenizer.Tokenizer;



public class Dataset extends Converter {
    
    public enum TokenizerMode {
        TRAINING,
        TESTING
    }

    private String resourceAddr = "";
    private Tokenizer tok;
    private TokenizerMode mode;

    public Dataset(String resourceAddr, Tokenizer tok, TokenizerMode mode) {
        this.resourceAddr = resourceAddr;
        this.tok = tok;
        this.mode = mode;
    }   

    public void make() throws FileNotFoundException, IOException {

        switch (mode) {
            case TRAINING:
                makeTrainSet();

                break;
            case TESTING:
                makeTestSet();
                break;
        }
        
    }   

    private void makeTrainSet() throws FileNotFoundException, IOException {

        File sourceDir, projectDir;
        // StringBuilder csv;
        CSVPrinter csvPrinter;
        // BufferedWriter writer;
        File outfile;
        

        /* add lineblocks into 'lineBlocks' for each files. */
        sourceDir = new File(resourceAddr + "train_resource/");
        for (String projectName : sourceDir.list()) {
            projectDir = new File(resourceAddr + "train_resource/" + projectName);
            // csv = new StringBuilder();
            // csv.append("Line1,Line2,Line3,Line4,Line5,Buggy\n");
            outfile = new File(resourceAddr + "dataset/train/" + projectName + "/data.csv");
            outfile.getParentFile().mkdirs();
            csvPrinter = new CSVPrinter(new FileWriter(outfile), CSVFormat.DEFAULT.withHeader("Line1", "Line2", "Line3", "Line4", "Line5", "Buggy"));
            try (ProgressBar pb = new ProgressBar(String.format("%15s", projectName), projectDir.list().length)) {
                for (String filename : projectDir.list()) {
                    makeOneProjectTrainSet("train_resource/" + projectName + "/" + filename, csvPrinter);
                    pb.step();
                }
                pb.setExtraMessage("...Done!!");
            }
            
            // writer = new BufferedWriter(new FileWriter(outfile, false));
            // writer.write(csv.toString());
            // writer.close();

            outfile = new File(resourceAddr + "output/train/" + projectName + "/data.csv");
            outfile.getParentFile().mkdirs();
            tok.tokenize(
                "dataset/train/" + projectName + "/data.csv", 
                "output/train/" + projectName + "/data.csv",
                projectName);
        }
	}

    private void makeOneProjectTrainSet(String inputFileName, CSVPrinter csvPrinter) {
        File inputFile;
        BufferedReader br;

        if (!inputFileName.endsWith(".java")) 
            return ;
        try {
            ArrayList<String> code;
            String[] tempCodeArray;
            String line;

            /* set the file stream to read */
            inputFile = new File(resourceAddr + inputFileName);
            br = new BufferedReader(new FileReader(inputFile));

            /* read the line in the file & store in 'code' */
            code = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (isCommentLine(line)) {
                    continue;
                }
                
                code.add(line);
            }
            
            /* convert 'code' to array */
            tempCodeArray = (String[]) code.toArray(new String[0]);

            // csvPrinter.printRecord(tempCodeArray[0], tempCodeArray[1], tempCodeArray[2], tempCodeArray[3], tempCodeArray[4], false);
            for (int i = 3; i < tempCodeArray.length-2; i++) {
                csvPrinter.printRecord(tempCodeArray[i-2], tempCodeArray[i-1], tempCodeArray[i], tempCodeArray[i+1], tempCodeArray[i+2], false);
            }
            
            /* add new lineblocks into 'lineBlocks' */
            // for (int i = 3; i < tempCodeArray.length-2; i++) {
            //     csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",false\n", 
            //                                     tempCodeArray[i-2], tempCodeArray[i-1], tempCodeArray[i],
            //                                     tempCodeArray[i+1], tempCodeArray[i+2]));
            // }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeTestSet() throws FileNotFoundException, IOException {

        File sourceDir, projectDir;
        File outPath;

        sourceDir = new File(resourceAddr + "test_resource/");

        for (String projectName : sourceDir.list()) {
            outPath = new File(resourceAddr + "output/test/" + projectName + "/data.csv");  
            outPath.getParentFile().mkdirs();
            
            projectDir = new File(resourceAddr + "test_resource/" + projectName);
            try (ProgressBar pb = new ProgressBar(String.format("%15s", projectName), projectDir.list().length)) {
                for (String filename : projectDir.list()) {
                    tok.tokenize(
                        "test_resource/" + projectName + "/" + filename, 
                        "output/test/" + projectName + "/" + filename,
                        projectName
                    );
                    pb.step();
                }
                pb.setExtraMessage("...Done!!");
            }
        }
    }

    // private void makeTestSet() throws FileNotFoundException, IOException {

    //     /* Read all csv file in './src/main/resources/test_resource/ 
    //     and concatenate in one csv file*/        
    //     File sourceDir, projectDir;
    //     StringBuilder csv;
    //     BufferedWriter writer;
    //     File outfile;

    //     /* add lineblocks into 'lineBlocks' for each files. */  
    //     sourceDir = new File(resourceAddr + "test_resource/");
    //     for (String projectName : sourceDir.list()) {
    //         projectDir = new File(resourceAddr + "test_resource/" + projectName);
    //         csv = new StringBuilder();
    //         csv.append("Line1,Line2,Line3,Line4,Line5,Buggy\n");
    //         try (ProgressBar pb = new ProgressBar(String.format("%15s", projectName), projectDir.list().length)) {
    //             // Read all csv file in projectDir.list() and concatenate in one csv file
    //             for (String filename : projectDir.list()) {
    //                 // Read one csv file and append to csv
    //                 File inputFile = new File(resourceAddr + "test_resource/" + projectName + "/" + filename);
    //                 BufferedReader br = new BufferedReader(new FileReader(inputFile));
    //                 String line;
    //                 // Skip first line
    //                 br.readLine();

    //                 while ((line = br.readLine()) != null) {
    //                     // substring of line to remove prefix upto first comma
    //                     line = line.substring(line.indexOf(",") + 1);
    //                     csv.append(line + "\n");
    //                 }
    //                 pb.step();
    //             }
    //             pb.setExtraMessage("...Done!!");
    //         }
    //         outfile = new File(resourceAddr + "dataset/test/" + projectName + "/data.csv");
    //         outfile.getParentFile().mkdirs();
    //         writer = new BufferedWriter(new FileWriter(outfile, false));
    //         writer.write(csv.toString());
    //         writer.close();

    //         outfile = new File(resourceAddr + "output/test/" + projectName + "/data.csv");
    //         outfile.getParentFile().mkdirs();
    //         tok.tokenize(
    //             "dataset/test/" + projectName + "/data.csv", 
    //             "output/test/" + projectName + "/data.csv");
    //     }
    // }


    // private void makeTestSet() throws FileNotFoundException, IOException {

    //     File sourceDir, projectDir;

    //     /* add lineblocks into 'lineBlocks' for each files. */
    //     sourceDir = new File(resourceAddr);
    //     System.out.println("[Convert the text csv file into the tokenized csv]");
    //     System.out.println("Starting ...");
    //     for (String projectName : sourceDir.list()) {
    //         projectDir = new File(resourceAddr + projectName);
    //         try (ProgressBar pb = new ProgressBar(String.format("%15s", projectName), projectDir.list().length)) {
    //             for (String fileName : projectDir.list()) {
    //                 tok.tokenize("testset/", projectName + "/test/", fileName);
    //                 pb.step();
    //             }
    //             pb.setExtraMessage("...Done!!");
    //         }
    //     }
    // }
}







