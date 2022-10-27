package edu.handong.csee.isel.data.collector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.data.collector.core.GitHubSearcher;

public class Test {
    public static void main(String[] args) {
        try {
            GitHubSearcher searcher = new GitHubSearcher("C:/activemq/.git");
            
            for (RevCommit commit 
                    : searcher.getSplittingCommit(0.6F, null, null)
                            .getParents()) {
                System.out.println(commit.getAuthorIdent().getWhen());
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(NoHeadException e) {
            //e.printStackTrace();
        } catch(GitAPIException e) {
            e.printStackTrace();
        }
        
    }
}
