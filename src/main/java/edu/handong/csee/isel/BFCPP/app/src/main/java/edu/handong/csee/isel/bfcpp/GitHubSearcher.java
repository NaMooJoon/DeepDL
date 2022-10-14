package edu.handong.csee.isel.bfcpp;

import java.util.ArrayList;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitSearchBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedSearchIterable;

import java.io.IOException;

/**
 * Searches commit hash from given repository.
 */
public class GitHubSearcher {
    private GHCommitSearchBuilder builder;

    /**
     * @param token github authentication token
     * @param repoName repository name
     * @throws IOException
     */
    public GitHubSearcher(String token, String repoName) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();

        builder = github.searchCommits().repo(repoName);
    }

    /**
     * @param issueNum issue number for commit of searching hashcode 
     * @return array list of hashcode of corresponding commit
     * @throws IOException
     */
    public ArrayList<String> search(String issueNum) throws IOException{
        PagedSearchIterable<GHCommit> commits = builder.q(issueNum).list();
        ArrayList<String> hashes = new ArrayList<>();

        for (GHCommit commit : commits) {
            if (commit.getCommitShortInfo().getMessage()
                    .matches("[^0-9]" + issueNum + "[^0-9]")) {
                hashes.add(commit.getSHA1());
            }
        }
        return hashes;
    }
}
