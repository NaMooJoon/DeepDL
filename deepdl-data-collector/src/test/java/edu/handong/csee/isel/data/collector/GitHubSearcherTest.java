package edu.handong.csee.isel.data.collector;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.handong.csee.isel.data.collector.core.GitHubSearcher;

class GitHubSearcherTest {

    @Test
    private void testCloneRepository() {
        GitHubSearcher searcher = new GitHubSearcher();
        String targetPath = String.join(System.getProperty("file.separator"), 
                                        System.getProperty("user.dir"),  
                                        "src", "test", "java", 
                                        "edu", "handong", "csee", "isel",
                                        "data", "collector", "target");
        File repository = new File(targetPath, ".github");
        
        try {
            searcher.cloneRepository("https://github.com/apache/juddi", 
                                     targetPath);
        } catch(Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(repository.exists());
    }
}
