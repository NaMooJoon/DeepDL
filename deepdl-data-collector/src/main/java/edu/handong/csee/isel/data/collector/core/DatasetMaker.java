package edu.handong.csee.isel.data.collector.core;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.json.JSONObject;

/**
 * Class that makes dataset for DeepDL model.
 */
public class DatasetMaker {
    private final int FILE_BEGIN_INDEX = 6;
    private final int LINE_BEGIN_INDEX = 1;

    private GitHubSearcher searcher;

    /**
     * Sets <code>GitHubSearcher</code> to this instance.
     */
    public DatasetMaker(GitHubSearcher searcher) {
        this.searcher = searcher;
    }

    /**
     * Makes clean snapshots of the given repository with BIC before the given splitting point and writes commit JSON files with pinpointed buggy lines of the given repository with BIC after the given splitting point.
     * The files in the <code>projectPath</code>/out/snapshot will be modified to make clean clean snapshots.
     * The commit JSON files will be written at <code>projectPath</code>/out/test-data. 
     * @param splittingPoint the splitting point
     */
    public void makeDataset(Date splittingPoint) {
        


    }

    /**
     * Makes clean snapshot by using the BIC and BFC from the given JSON object. 
     * The files in the <code>projectPath</code>/out/snapshot will be modified.
     * @param jo the JSON object
     */
    private void makeCleanSnapshot(JSONObject jo) {
        
    }

    /**
     * Writes BIC with pinpointed buggy lines in JSON format by using the BIC and BFC from the given JSON object.
     * The file will be written at <code>projectPath</code>/out/test-data.
     * @param jo the JSON object
     */
    private void saveBICWithPinpointedBuggyLines(JSONObject jo) {

    }

    /**
     * Gets added lines of the given commit hash.
     * @param hash the commit hash
     * @return added lines of the given commit which is classified by files
     * @throws GitAPIException 
     * @throws MissingObjectException
     * @throws IOException
     */
    private HashMap<String, ArrayList<String>> getAddedLines(String hash) 
            throws GitAPIException, MissingObjectException, IOException {
        String[] lines = searcher.diffCommits(
                                searcher.convertHashToPreviousCommit(hash),
                                searcher.convertHashToCommit(hash));
        String file = null;                 
        HashMap<String, ArrayList<String>> addedLines = new HashMap<>(); 

        for (String line : lines) {
            if (line.startsWith("+++ b/")) {
                file = line.substring(FILE_BEGIN_INDEX);

                addedLines.put(file, new ArrayList<String>());
            } else if (line.startsWith("+")) {
                addedLines.get(file).add(line.substring(LINE_BEGIN_INDEX));
            }
        }
        return addedLines;
    }

    /**
     * Gets removed lines of the given commit.
     * @param commit the commit
     * @return removed lines of the given commit which is classified by files
     * @throws GitAPIException
     * @throws MissingObjectException
     * @throws IOException
     */
    private HashMap<String, ArrayList<String>> getRemovedLines(String hash) 
            throws GitAPIException, MissingObjectException, IOException {
        String[] lines = searcher.diffCommits(
                                searcher.convertHashToPreviousCommit(hash), 
                                searcher.convertHashToCommit(hash));
        String file = null;                 
        HashMap<String, ArrayList<String>> removedLines = new HashMap<>(); 

        for (String line : lines) {
            if (line.startsWith("--- a/")) {
                file = line.substring(FILE_BEGIN_INDEX);

                removedLines.put(file, new ArrayList<String>());
            } else if (line.startsWith("-")) {
                removedLines.get(file).add(line.substring(LINE_BEGIN_INDEX));
            }
        }
        return removedLines;
    }

    /**
     * Gets buggy lines by comparing the given added lines and removed lines.
     * @param addedLines the added lines
     * @param removedLines the removed lines
     * @return buggy lines which is classified by files
     */
    private HashMap<String, ArrayList<String>> getBuggyLines(
            HashMap<String, ArrayList<String>> addedLines, 
            HashMap<String, ArrayList<String>> removedLines) {
        Set<String> keys = addedLines.keySet();
        HashMap<String, ArrayList<String>> buggyLines = new HashMap<>();
        
        for (String key : keys) {
            ArrayList<String> removedVal = removedLines.get(key);

            if (removedVal != null) {
                ArrayList<String> addedVal = addedLines.get(key);
                ArrayList<String> buggyVal = new ArrayList<>();
                
                for (String addedLine : addedVal) {
                    for (String removedLine : removedVal) {
                        if (removedLine.equals(addedLine)) {
                            buggyVal.add(addedLine);

                            break;
                        }
                    }
                }
                buggyLines.put(key, buggyVal);
            }
        }
        return buggyLines;
    } 

    /**
     * Removes the given buggy lines from the given file.
     * @param file the file
     * @param buggyLines the buggy lines
     */
    private void removeBuggyLines(String file, ArrayList<String> buggyLines) {
        




    }
}
