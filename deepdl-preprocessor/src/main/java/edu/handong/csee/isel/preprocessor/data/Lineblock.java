package edu.handong.csee.isel.preprocessor.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.tongfei.progressbar.*;

public class Lineblock extends Converter {

    private String resourceAddr = "";

    public Lineblock(String resourceAddr) {
        this.resourceAddr = resourceAddr;
    }

    public void make() throws FileNotFoundException, IOException {
        File sourceDir, projectDir;
        ArrayList<ArrayList<String>> lineBlocks;
        Gson gson;
        String json;
        BufferedWriter writer;
        

        /* initialize the file and close */
        new FileOutputStream(resourceAddr + "lineblocks.json", false).close(); 

        /* add lineblocks into 'lineBlocks' for each files. */
        sourceDir = new File(resourceAddr + "trainset_resource/");
        lineBlocks = new ArrayList<ArrayList<String>>();
        System.out.println("[Generate Line Block]");
        System.out.println("Starting to make line blocks from the clean code!");
        for (String projectName : sourceDir.list()) {
            projectDir = new File(resourceAddr + "trainset_resource/" + projectName);
            try (ProgressBar pb = new ProgressBar(String.format("%15s", projectName), projectDir.list().length)) {
                for (String fileName : projectDir.list()) {
                    append("trainset_resource/" + projectName + '/' + fileName, lineBlocks); /* add new file data into 'lineBlocks' */
                    pb.step();
                }
                pb.setExtraMessage("...Done!!");
            }
        }

        /* write the file in JSON form. */
        gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        json = gson.toJson(lineBlocks);
        writer = new BufferedWriter(new FileWriter(resourceAddr + "lineblocks.json", true));
        writer.write(json);
        writer.close();

    }

    private void append(String inputFileName, ArrayList<ArrayList<String>> lineBlocks) {
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
            
            /* add new lineblocks into 'lineBlocks' */
            for (int i = 3; i < tempCodeArray.length-2; i++) {
                ArrayList<String> block = new ArrayList<>();

                block.add("[SOS]" + tempCodeArray[i-2] + "[EOL]");
                block.add(tempCodeArray[i-1] + "[EOL]");
                block.add(tempCodeArray[i-0] + "[EOL]");
                block.add(tempCodeArray[i+1] + "[EOL]");
                block.add(tempCodeArray[i+2] + "[EOS]");

                lineBlocks.add(block);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
