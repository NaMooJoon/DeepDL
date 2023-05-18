package edu.handong.csee.isel.preprocessor.tokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Dictionary {

    public void encodeDictionary(String resourcesAddress, String corpusDataFile) throws IOException, InterruptedException {

        
    }
    /**
     * Before encoding the dictionary of BPE, to prepare 
     * the input data of python-BPE, make one code-concatenated file, 
     * which is from multiple java files.
     * @param sourceDir a local directory route, where are the java files for training.
     * @param destination a local directory route, where the result file is located.
     */
    public void prepareCorpusDataFile(String inputAddress, String outputAddress) throws IOException, InterruptedException {
        try {
            File dir = new File(inputAddress);
            String outputFileName = "corpus";
            new FileOutputStream(outputAddress+outputFileName, false).close();
            for (String javaFile : dir.list()) {
                // appendCorpusDataFile(inputAddress, javaFile, outputAddress, outputFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }// catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
    }
   
    public void appendCorpusDataFile(String inputAddress, String inputFileName, String outputAddress, String outputFileName) throws IOException, InterruptedException {
        try {
            FileInputStream inputFile = new FileInputStream(inputAddress+inputFileName);
            FileOutputStream outputFile = new FileOutputStream(outputAddress+outputFileName, true);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputFile.read(buf, 0, 1024)) > 0) {
                outputFile.write(buf, 0, len);
            }
            inputFile.close();
            outputFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
