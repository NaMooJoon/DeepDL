package edu.handong.csee.isel.data.collector;

import java.util.ArrayList;

//import edu.handong.csee.isel.data.collector.io.CommitHashReader;
//import edu.handong.csee.isel.data.collector.io.PropertyWriter;
import edu.handong.csee.isel.data.collector.github.GitHubSearcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

public class Test {
    public static void main(String[] args) {
        try {
            /*
            String[] projects = { "juddi" };
            ArrayList<String>[] hashes = new ArrayList[1];

            hashes[0] = new CommitHashReader("C:/PATCH_juddi.csv")
                    .readCommitHashes(projects[0], "2004-01-01", "2022-12-31");
    
            new PropertyWriter("test.json").writePySZZJson(projects, hashes);
            */
            System.out.println(new GitHubSearcher("C:/activemq").getSplittingCommit().getRawBuffer());
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(NoHeadException e) {
            e.printStackTrace();
        } catch(GitAPIException e) {
            e.printStackTrace();
        }
    }
}
