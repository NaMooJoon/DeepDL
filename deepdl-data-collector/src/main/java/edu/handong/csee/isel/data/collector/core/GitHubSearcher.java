package edu.handong.csee.isel.data.collector.core;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

/**
 * Class that gets essential ingredients for DeepDL data collection from GitHub. 
 */
public class GitHubSearcher {
    private Git git;
    
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
     * @param startDate start date - yyyy-[m]m-[d]d
     * @param endDate end date - yyyy-[m]m-[d]d
     * @return splitting commit
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
     * Changes this instance's git repository
     * @param gitDir a local git repository metadata directory
     * @throws IOException
     */
    public void changeRepository(String gitDir) throws IOException {
        git.close();
        
        git = Git.open(new File(gitDir));
    }

    /**
     * Closes this instance.
     */
    public void close() {
        git.close();
    }
}