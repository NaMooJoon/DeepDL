package edu.handong.csee.isel.data.collector.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import edu.handong.csee.isel.data.collector.util.Utils;

/**
 * Class that gets data from GitHub. 
 */
public class GitHubSearcher implements AutoCloseable {
    private Git git;
    private String repouser;
    private String repository;

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
        new CloneCommand().setURI(uri)
                          .setDirectory(new File(dir))
                          .call()
                          .close();
    }

    /**
     * Gets diff between the given two commits.
     * The given commits should be in this instance's object database.
     * @param oldCommit the old commit
     * @param newCommit the new commit
     * @return diff context between the <code>oldCommit</code> and <code>newCommit</code>
     * @throws GitAPIException
     * @throws IncorrectObjectTypeException
     * @throws MissingObjectException
     * @throws IOException
     */
    public String[] diffCommits(RevCommit oldCommit, RevCommit newCommit) 
            throws GitAPIException, IncorrectObjectTypeException, 
                   MissingObjectException, IOException { 
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectReader or = git.getRepository().newObjectReader()) {
            git.diff()
               .setOutputStream(out)
               .setOldTree(new CanonicalTreeParser(null, 
                                                   or, 
                                                   oldCommit.getTree()))
               .setNewTree(new CanonicalTreeParser(null, 
                                                   or,
                                                   newCommit.getTree()))
               .call();
          
            return out.toString().split("\n");
        }
    }   
    
    /**
     * Checkout to the snapshot of this instance's repository.
     * The snapshot of the project refers to the latest commit state before the splitted commit.
     * @param splittedCommit the splitted commit
     * @throws GitAPIException 
     */
    public void checkoutToSnapshot(RevCommit splittedCommit) 
            throws GitAPIException {
        git.checkout()
           .setCreateBranch(true)
           .setName("snapshot")
           .setStartPoint(splittedCommit.getParents()[0])
           .call();
    }

    /**
     * Gets splitting commit from this instance's repository.
     * The splitting commit splits this instance's repository's commits from [<code>startDate</code>, <code>endDate</code>) in ratio of <code>trainRatio</code> : 1 - <code>trainRatio</code>.
     * The <code>startDate</code> and <code>endDate</code> should follow JDBC date escape format.
     * @param trainRatio a ratio of training commits
     * @param startDate start date of the repository's commit - yyyy-[M]M-[d]d
     * @param endDate end date of the repository's commit - yyyy-[M]M-[d]d
     * @return the splitting commit
     * @throws GitAPIException
     * @throws NoHeadException
     * @throws IOException
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
     * Converts the given commit hash into the <code>RevCommit</code> instance.
     * The given commit hash should be in this instance's object database.
     * @param hash the commit hash
     * @return the <code>RevCommit</code> instance 
     * @throws MissingObjectException
     * @throws IncorrectObjectTpeException
     * @throws IOException
     */
    public RevCommit convertHashToCommit(String hash) 
            throws MissingObjectException, IncorrectObjectTypeException, 
                   IOException {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            return revWalk.parseCommit(RevCommit.fromString(hash));  
        }
    }

    /**
     * Converts the given commit hash into the previous <code>RevCommit</code> instance.
     * The given commit hash should be in this instance's object database.
     * @param hash the commit hash
     * @return the previous <code>RevCommit</code> instance
     * @throws MissingObjectException
     * @throws IncorrectObjectTypeException
     * @throws IOExcpetion
     */
    public RevCommit convertHashToPreviousCommit(String hash) 
            throws MissingObjectException, IncorrectObjectTypeException, 
                   IOException {
        RevWalk revWalk = new RevWalk(git.getRepository());
        RevCommit commit = revWalk.parseCommit(RevCommit.fromString(hash))
                                  .getParent(0);        
        revWalk.parseCommit(commit.getId());
        revWalk.close();

        return commit;
    }

    /**
     * Changes this instance's <code>Git</code> instance with the given git metadata directory.
     * The repouser and repository of the given git metadata directory is set.  
     * @param gitDir the git metadata directory
     * @throws IOException
     */
    public void changeRepository(String gitDir) throws IOException {
        String[] nameElements;
        String regex;
        
        if (git != null) {
            git.close();
        }
         
        git = Git.open(new File(gitDir));
        regex = File.separator.equals("\\") ? "\\\\" : File.separator; 
        nameElements = gitDir.split(regex);

        for (int i = 0; i < nameElements.length; i++) {
            if (nameElements[i].equals(Utils.PROJECT_DIR)) {
                repouser = nameElements[i + 3];
                repository = nameElements[i + 4];

                break;
            }
        }
    }

    @Override
    public void close() {
        git.close();
    }

    public String getRepouser() {
        return repouser;
    }

    public String getRepository() {
        return repository;
    }
}