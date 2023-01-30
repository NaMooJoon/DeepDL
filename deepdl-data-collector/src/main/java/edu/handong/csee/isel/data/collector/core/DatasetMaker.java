package edu.handong.csee.isel.data.collector.core;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Class that makes dataset for DeepDL model.
 */
public class DatasetMaker {
    private final int FILE_BEGIN_INDEX = 6;
    private final int LINE_BEGIN_INDEX = 1;

    /**
     * Makes clean snapshots of the given repository with BIC before the given splitting point and writes commit JSON files with pinpointed buggy lines of the given repository with BIC after the given splitting point.
     * The files in the <code>projectPath</code>/out/snapshot will be modified to make clean clean snapshots.
     * The commit JSON files will be written at <code>projectPath</code>/out/test-data. 
     * @param repository the repository
     * @param splittingPoint the splitting point
     */
    public void makeDataset(String repository, Date splittingPoint) {
        


    }

    /**
     * Makes clean snapshot by using the given BIC. 
     * The files in the <code>projectPath</code>/out/snapshot will be modified.
     * @param bic the BIC
     */
    private void makeCleanSnapshot(String bic) {

    }

    /**
     * Writes BIC with pinpointed buggy lines in JSON format by using the given BIC.
     * The file will be written at <code>projectPath</code>/out/test-data.
     * @param bic the BIC
     */
    private void saveBICWithPinpointedBuggyLines(String bic) {

    }

    /**
     * Gets added lines of the given commit.
     * @param commit the commit
     * @return added lines of the given commit which is classified by the file name
     */
    private HashMap<String, ArrayList<String>> getAddedLines(
            Repository repo, String commit) 
                    throws MissingObjectException, IOException {
        HashMap<String, ArrayList<String>> addedLines = new HashMap<>(); 
        String[] lines = new String(repo.newObjectReader()
                                        .open(RevCommit.fromString(commit))
                                        .getCachedBytes())
                         .split("\n");
        String file = null;                 

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
     * @return removed lines of the given commit which is classified by the file name
     */
    private HashMap<String, ArrayList<String>> getRemovedLines(
                Repository repo, String commit) 
                    throws MissingObjectException, IOException {
        HashMap<String, ArrayList<String>> removedLines = new HashMap<>(); 
        String[] lines = new String(repo.newObjectReader()
                                        .open(RevCommit.fromString(commit))
                                        .getCachedBytes())
                         .split("\n");
        String file = null;                 

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
}
