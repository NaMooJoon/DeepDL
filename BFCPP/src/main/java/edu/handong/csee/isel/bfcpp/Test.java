package edu.handong.csee.isel.bfcpp;

import org.kohsuke.github.*;
import org.kohsuke.github.GHCommitBuilder;
import org.kohsuke.github.GitHubBuilder;


public class Test {
    public static void main(String[] args) {
        /* 
        try {
            String fileName = "../../../../../../DPMinerTest/patch/apacheAMQ/apacheAMQ.csv";
            String token = "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";

            ArrayList<String> issueNums = new IssueNumberReader().read(fileName);
            ArrayList<String> hashes = new GitHubSearcher(token, Repository.repos[0]).search(issueNums.get(0));

            System.out.println(hashes.get(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        try {
            String token = "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";
            GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        } catch (Exception e) {}          
    }    
}
