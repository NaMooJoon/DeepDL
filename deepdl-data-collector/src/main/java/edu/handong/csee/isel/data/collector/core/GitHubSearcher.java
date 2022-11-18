package edu.handong.csee.isel.data.collector.core;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;

/**
 * Class that gets essential ingredients for DeepDL data collection from GitHub. 
 */
public class GitHubSearcher {
    private Git git;
    
    public GitHubSearcher() {}

    /**
     * Creates <code>Git</code> object for using git commands. 
     * @param gitDir a local git repository metadata directory 
     * @throws IOException
     */
    public GitHubSearcher(String gitDir) throws IOException {
        git = Git.open(new File(gitDir));
    }

    /**
     * Gets splitting commit from this instance's repository.
     * The splitting commit splits this instance's repositoriy's commits from [<code>startDate</code>, <code>endDate</code>) in ratio of <code>trainRatio</code> : 1 - <code>trainRatio</code>.
     * The <code>startDate</code> and <code>endDate</code> should follow JDBC date escape format.
     * @param trainRatio a ratio of training commits
     * @param startDate start date of the repository's commit - yyyy-[m]m-[d]d
     * @param endDate end date of the repository's commit - yyyy-[m]m-[d]d
     * @return the splitting commit
     */
    public RevCommit getSplittingCommit(float trainRatio, 
            String startDate, String endDate) 
                    throws GitAPIException, NoHeadException, IOException {  
        final long LOWER_BOUND = 0L;
        final long UPPER_BOUND = 1000L * 60 * 60 * 24 * 365 * 60;           

        Date start = startDate != null ? Date.valueOf(startDate) 
                                       : new Date(LOWER_BOUND);     
        Date end = endDate != null ? Date.valueOf(endDate) 
                                   : new Date(UPPER_BOUND);
        ArrayList<RevCommit> commits = new ArrayList<>();
        
        for (RevCommit commit : git.log().all().call()) {
            java.util.Date commitDate = commit.getAuthorIdent().getWhen();

            if (start.before(commitDate) && end.after(commitDate)) {
                commits.add(commit);
            }
        }
        return commits.get((int) ((commits.size() - 1) * (1 - trainRatio)));
    }

    /**
     * Checkout to the snapshot of this instance's repository.
     * The snapshot of the project refers to the latest commit state before the splitted commit.
     * @param splittedCommit the splitted commit
     * @throws GitAPIException 
     */
    public void CheckoutToSnapshot(RevCommit splittedCommit) 
            throws GitAPIException {
        git.checkout().setStartPoint(splittedCommit.getParents()[0]).call();
    }

     /**
     * Clones the repository to the given directory.
     * Creates the directory by creating all of the nonexistent parent directory if there is no given directory.
     * @param uri GitHub respository uri
     * @param dir the directory
     * @throws InvaildRemoteException
     * @throws TransportException
     * @throws GitAPIException
     */
    public void cloneRepository(String uri, String dir) 
            throws InvalidRemoteException, 
                    TransportException, GitAPIException {
        File directory = new File(dir);
        CloneCommand cloneCommand = new CloneCommand();

        directory.mkdirs();
        cloneCommand.setURI(uri).setDirectory(directory).call().close();
    }

    /**
     * Changes this instance's git repository
     * @param gitDir a local git repository metadata directory
     * @throws IOException
     */
    public void changeRepository(String gitDir) throws IOException {
        if (git != null) {
            git.close();
        }
        git = Git.open(new File(gitDir));
    }

    /**
     * Closes this instance.
     */
    public void close() {
        git.close();
    }
}