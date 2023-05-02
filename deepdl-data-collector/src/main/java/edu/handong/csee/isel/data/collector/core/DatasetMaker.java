package edu.handong.csee.isel.data.collector.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat.Builder;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.MissingObjectException;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.handong.csee.isel.data.collector.util.Utils;

/**
 * Class that makes dataset for DeepDL model.
 */
public class DatasetMaker {
    public static final int CENTRAL_LINE = 3;
    public static final int FILE_BEGIN_INDEX = 6;
    public static final int LINE_BEGIN_INDEX = 1;

    private GitHubSearcher searcher;

    /**
     * Sets <code>GitHubSearcher</code> to this instance.
     */
    public DatasetMaker(GitHubSearcher searcher) {
        this.searcher = searcher;
    }

    /**
     * Makes clean snapshots of the current repository by using BIC before the given splitting point 
     * and writes BIC files with pinpointed buggy lines of the current repository by using BIC after the given splitting point.<p>
     * The files in the <code>projectPath</code>/out/snapshot is modified to make clean snapshots.<p>
     * The BIC files with pinpointed buggy lines is written at <code>projectPath</code>/out/test-data. 
     * @param reponmae name of the current repository
     * @param splittingPoint the splitting point
     * @throws MissingObjectException
     * @throws IOException
     * @throws GitAPIException
     */
    public void makeDataset(String reponame, Date splittingPoint) 
            throws MissingObjectException, IOException, GitAPIException {
        JSONArray ja = new JSONArray(
                Files.readAllLines(Path.of(Utils.getProjectPath(), 
                                   "out", "bic", "bic_" + reponame + ".json"))
                .get(0));
        HashMap<String, HashMap<String, ArrayList<ArrayList<Object>>>> 
                records = new HashMap<>();

        for (Object o : ja) {
            JSONObject jo = (JSONObject) o; 
            HashMap<String, ArrayList<String>> removedLines = 
                    getRemovedLines(jo.getString("fix_commit_hash"), "java");
            
            for (Object p : jo.getJSONArray("inducing_commit_hash")) {
                String hash = (String) p;

                if (searcher.convertHashToCommit(hash)
                            .getAuthorIdent()
                            .getWhen()
                            .after(splittingPoint)) {
                    if (records.containsKey(hash)) {
                        updateRecords(records.get(hash), removedLines);
                    } else {
                        records.put(hash, makeRecords(
                                getAddedAndMaintainedLines(hash, "java"), 
                                removedLines));
                    }
                } else {
                    makeCleanSnapshot(getBuggyLines(
                            getAddedLines(hash, "java"), 
                            removedLines));
                }
            }
        }

        for (Entry<String, HashMap<String, ArrayList<ArrayList<Object>>>> entry
                : records.entrySet()) {
            saveBICWithPinpointedBuggyLines(reponame, entry.getKey(), entry.getValue());
        }        
    }

    /**
     * Removes given buggy lines from the snapshots.
     * The files in the <code>projectPath</code>/out/snapshot is modified.
     * @param buggyLines the buggy lines
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void makeCleanSnapshot(
            HashMap<String, ArrayList<String>> buggyLines) 
                    throws FileNotFoundException, IOException { 
        for (String key : buggyLines.keySet()) {
            removeBuggyLines(String.join(File.separator, 
                                         Utils.getProjectPath(), 
                                         "out", "snapshot", 
                                         searcher.getRepouser(), key), 
                             buggyLines.get(key));
        }
    }

    /**
     * Makes records with the given added and maintained lines of BIC and removed lines of BFC.
     * @param addedAndMaintainedLines the added and maintained lines
     * @param removedLines the removed lines
     * @return records which are classified by files
     */
    private HashMap<String, ArrayList<ArrayList<Object>>> makeRecords(
            HashMap<String, ArrayList<String>> addedAndMaintainedLines,
            HashMap<String, ArrayList<String>> removedLines) {        
        HashMap<String, ArrayList<ArrayList<Object>>> records = 
                new HashMap<>();

        for (String key : addedAndMaintainedLines.keySet()) {
            ArrayList<String> addedAndMaintainedList = 
                    addedAndMaintainedLines.get(key);
            ArrayList<String> removedList = removedLines.containsKey(key) 
                    ? removedLines.get(key) 
                    : null;
            ArrayList<ArrayList<Object>> recordList = new ArrayList<>();
            
            for (int i = 0; i < addedAndMaintainedList.size(); i++) {
                if (addedAndMaintainedList.get(i).startsWith("+")) {
                    ArrayList<Object> record = new ArrayList<>();
                    
                    record.add(key);
                    
                    for (int j = i - 2; j <= i + 2; j++) {
                        if (j < 0 || j > addedAndMaintainedList.size() - 1) {
                            record.add("");
                        } else {
                            record.add(addedAndMaintainedList.get(j)
                                    .substring(LINE_BEGIN_INDEX));
                        }
                    }

                    record.add(removedList == null 
                            ? false 
                            : removedList.contains(record.get(CENTRAL_LINE)));
                    recordList.add(record);
                }   
            }
            
            if (!recordList.isEmpty()) {
                records.put(key, recordList);
            }
        }
        
        return records;    
    }
    
    /**
     * Updates the given records by comparing central line with the given removed lines of BFC.
     * @param records the records
     * @param removedLines the removed lines
     */
    private void updateRecords(
            HashMap<String, ArrayList<ArrayList<Object>>> records, 
            HashMap<String, ArrayList<String>> removedLines) {
        final int LABEL = 6;

        for (String key : records.keySet()) {
            if (removedLines.containsKey(key)) {
                ArrayList<ArrayList<Object>> recordList = records.get(key);
                ArrayList<String> removedList = removedLines.get(key);

                for (ArrayList<Object> record : recordList) {
                    if (removedList.contains((String) record.get(CENTRAL_LINE))) {
                        record.set(LABEL, true);    
                    }
                }
            }
        }
    }

    /**
     * Writes BIC with pinpointed buggy lines in CSV format.<p>
     * The file is written at <code>projectPath</code>/out/test-data.
     * @param reponame current repository name
     * @param bic BIC hash
     * @param records records
     * @throws IOException
     */
    private void saveBICWithPinpointedBuggyLines(String reponame, String bic,
            HashMap<String, ArrayList<ArrayList<Object>>> records) 
                    throws IOException {
        try (CSVPrinter printer = new CSVPrinter(
                new FileWriter(new File(String.join(File.separator, 
                                                    Utils.getProjectPath(), 
                                                    "out", "test-data", 
                                                    searcher.getRepouser(), 
                                                    reponame,
                                                    bic + ".csv"))), 
                Builder.create(CSVFormat.DEFAULT)
                       .setHeader("Filename", 
                                  "Line1", "Line2", "Line3", 
                                  "Line4", "Line5", 
                                  "Buggy")
                       .build())) {
            for (ArrayList<ArrayList<Object>> val : records.values()) {
                for (ArrayList<Object> record : val) {
                    printer.printRecord(record);
                }
            }
        }
    }

    /**
     * Gets buggy lines by comparing the given added lines and removed lines.
     * @param addedLines the added lines
     * @param removedLines the removed lines
     * @return buggy lines which are classified by files
     */
    private HashMap<String, ArrayList<String>> getBuggyLines(
            HashMap<String, ArrayList<String>> addedLines, 
            HashMap<String, ArrayList<String>> removedLines) {
        Set<String> keys = addedLines.keySet();
        HashMap<String, ArrayList<String>> buggyLines = new HashMap<>();
        
        for (String key : keys) {
            if (removedLines.containsKey(key)) {
                ArrayList<String> addedList = addedLines.get(key);
                ArrayList<String> removedList = removedLines.get(key);
                ArrayList<String> buggyList = new ArrayList<>();
                
                for (String addedLine : addedList) {
                    if (removedList.contains(addedLine)) {
                        buggyList.add(addedLine);
                    }
                }

                if (!buggyList.isEmpty()) {
                    buggyLines.put(key, buggyList);
                }
            }
        }

        return buggyLines;
    } 

    /**
     * Gets added lines of the given extension file from the given commit hash.<p>
     * '+' in front of the added lines is deleted.<p>
     * The file name and removed lines don't added to the return value if the file does not contain at least one added line.
     * @param hash the commit hash
     * @param extension the extension
     * @return added lines from the given commit hash which are classified by files
     * @throws GitAPIException 
     * @throws MissingObjectException
     * @throws IOException
     */
    private HashMap<String, ArrayList<String>> getAddedLines(String hash, 
            String extension) throws GitAPIException, MissingObjectException, 
                                     IOException {
        String[] lines = searcher.diffCommits(
                searcher.convertHashToPreviousCommit(hash),
                searcher.convertHashToCommit(hash));
        boolean isExtension = false;
        String filename = null;
        ArrayList<String> addedList = new ArrayList<>();                  
        HashMap<String, ArrayList<String>> addedLines = new HashMap<>(); 

        for (String line : lines) {
            if (line.startsWith("diff")) {
                if (!addedList.isEmpty()) {
                    addedLines.put(filename, addedList);

                    addedList = new ArrayList<>();
                }

                isExtension = line.endsWith("." + extension);
            } else if (isExtension && line.startsWith("+++")) {
                filename = line.substring(FILE_BEGIN_INDEX);
            } else if (isExtension && line.startsWith("+")) {
                addedList.add(line.substring(LINE_BEGIN_INDEX));
            }
        }

        if (!addedList.isEmpty()) {
            addedLines.put(filename, addedList);
        }

        return addedLines;
    }

    /**
     * Gets removed lines of the given extension file from the given commit hash.<p>
     * '-' in front of the removed lines is deleted.<p>
     * The file name and removed lines don't added to the return value if the file does not contain at least one removed line.
     * @param hash the commit hash
     * @param extension the extension 
     * @return removed lines from the given commit hash which are classified by files
     * @throws GitAPIException
     * @throws MissingObjectException
     * @throws IOException
     */
    private HashMap<String, ArrayList<String>> getRemovedLines(String hash, 
            String extension) throws GitAPIException, MissingObjectException, 
                                     IOException {
        String[] lines = searcher.diffCommits(
                searcher.convertHashToPreviousCommit(hash), 
                searcher.convertHashToCommit(hash));
        boolean isExtension = false;              
        String filename = null;   
        ArrayList<String> removedList = new ArrayList<>();
        HashMap<String, ArrayList<String>> removedLines = new HashMap<>(); 
       
        for (String line : lines) {
            if (line.startsWith("diff")) {
                if (!removedList.isEmpty()) {
                    removedLines.put(filename, removedList);
                    
                    removedList = new ArrayList<>();
                }
                
                isExtension = line.endsWith("." + extension);
            } else if (isExtension && line.startsWith("---")) {
                filename = line.substring(FILE_BEGIN_INDEX);
            } else if (isExtension && line.startsWith("-")) {
                removedList.add(line.substring(LINE_BEGIN_INDEX));
            }
        }

        if (!removedList.isEmpty()) {
            removedLines.put(filename, removedList);
        }

        return removedLines;
    }

    /**
     * Gets added and maintained lines of the given extension file from the given commit hash.<p>
     * '+' in front of the added lines is not deleted.<p>
     * ' ' in front of the maintained line is not deleted.<p>
     * The file name and added and maintained lines don't added to the return value if the file doesn't contain at least one added or maintained line. 
     * @param hash the commit hash
     * @param extension the extension
     * @return added and maintained lines of the given commit hash which are classified by files
     * @throws GitAPIException
     * @throws MissingObjectException
     * @throws IOException
     */
    private HashMap<String, ArrayList<String>> getAddedAndMaintainedLines(
            String hash, String extension) throws GitAPIException, 
                                                  MissingObjectException, 
                                                  IOException {
        String[] lines = searcher.diffCommits(
            searcher.convertHashToPreviousCommit(hash), 
            searcher.convertHashToCommit(hash));    
        boolean isExtension = false;
        String filename = null;   
        ArrayList<String> addedAndMaintainedList = new ArrayList<>();
        HashMap<String, ArrayList<String>> addedAndMaintainedLines = 
                new HashMap<>(); 

        for (String line : lines) {
            if (line.startsWith("diff")) {
                if (!addedAndMaintainedList.isEmpty()) {
                    addedAndMaintainedLines.put(filename, 
                                                addedAndMaintainedList);
                    
                    addedAndMaintainedList = new ArrayList<>();
                }

                isExtension = line.endsWith("." + extension);
            } else if (isExtension && line.startsWith("---")) {
                filename = line.substring(FILE_BEGIN_INDEX);
            } else if (isExtension 
                       && (line.startsWith("+") || line.startsWith(" "))
                       && !line.isBlank()) {
                addedAndMaintainedList.add(line);
            }
        }

        if (!addedAndMaintainedList.isEmpty()) {
            addedAndMaintainedLines.put(filename, addedAndMaintainedList);
        }

        return addedAndMaintainedLines;
    }

    /**
     * Removes the given buggy lines from the file of the given path name.
     * @param pathname the absolute path name 
     * @param buggyLines the buggy lines
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void removeBuggyLines(String pathname, 
                                  ArrayList<String> buggyLines) 
                                        throws FileNotFoundException, 
                                               IOException {
        final int THRESHOLD = 100;
                                                
        File file = new File(pathname);

        if (!file.exists()) {
            return;
        }

        ArrayList<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = 
                new BufferedReader(new FileReader(file))) {
            String line;
            System.out.printf("file: %s\n", pathname);
            int lineCount = 1;
            while ((line = reader.readLine()) != null) {
                if (!buggyLines.contains(line)) {
                    lines.add(line);
                    System.out.printf("buggy line: line %d %s\n", lineCount++, line);
                }
            }
        }

        try (BufferedWriter writer = 
                new BufferedWriter(new FileWriter(file))) {
            int numWritten = 0;
            
            for (String line : lines) {
                writer.write(line);
                writer.newLine();

                numWritten++;

                if (numWritten == THRESHOLD) {
                    writer.flush();

                    numWritten = 0;
                }
            }
        }
    }
}
