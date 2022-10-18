package edu.handong.csee.isel.data.collector.github;

import java.util.ArrayList;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

/**
 * Class that gets essential ingredients for DeepDL data collection from GitHub. 
 */
public class GitHubSearcher {
    private Git git;

<<<<<<< HEAD
    public void cloneSnapshot(String path, String branch) {

=======
    /**
     * Create <code>Git</code> object for using git commands. 
     * @param gitDir a local git repository directory 
     * @throws IOException
     */
    public GitHubSearcher(String gitDir) throws IOException {
        git = new Git(new FileRepository(gitDir));
    }

    /**
     * Gets splitting commit from the local repository.
     * @return splitting commit
     */
    public RevCommit getSplittingCommit() 
            throws GitAPIException, NoHeadException ,IOException {  
        final float TRAINING_RATIO = 0.6F;
        ArrayList<RevCommit> commits = new ArrayList<>();        
        
        for (RevCommit commit : git.log().all().call()) {
            commits.add(commit);
        }
        return commits.get((int) ((commits.size() - 1) * TRAINING_RATIO));
>>>>>>> brchA
    }

    public void cloneSnapshot(String path, String branch) {





    }
}