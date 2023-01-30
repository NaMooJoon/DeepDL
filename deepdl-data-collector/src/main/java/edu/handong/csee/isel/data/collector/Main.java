package edu.handong.csee.isel.data.collector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.data.collector.util.Utils;

public class Main {
    public static void main(String[] args) {
        
        try { 
            Git git = Git.open(new File(Utils.getProjectPath(), 
                               String.join(File.separator,
                                           "..", ".git")));
            CanonicalTreeParser oldParser = new CanonicalTreeParser();
            
            oldParser.reset(git.getRepository().newObjectReader(),
                            new RevWalk(git.getRepository())
                            .parseCommit(
                                    RevCommit.fromString(
                                            "22a1b41d47be5e6f9b5e3da7d621c93ac787d4f1"))
                            .getTree());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<DiffEntry> entries = git.diff()
                                         .setOutputStream(out)
                                         /** 
                                         .setOldTree(oldParser)
                                         .setNewTree(
                                                new CanonicalTreeParser(
                                                        null,
                                                        git.getRepository().newObjectReader(),
                                                        new RevWalk(git.getRepository())
                                                                .parseCommit(
                                                                        RevCommit.fromString(
                                                                                "414fc34653e1f14a3ad6781f524030f1216d0322"))
                                                                .getTree()))
                                            **/                       
                                         .call();
            
            System.out.println(new String(out.toByteArray()));

            DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);

            formatter.setReader(git.getRepository().newObjectReader(), new Config());

            for (DiffEntry entry : entries) {
                EditList edits = formatter.toFileHeader(entry).toEditList();
                
                for (Edit edit : edits) {
                    System.out.printf("a: %d - %d\n", edit.getBeginA(), edit.getEndA());
                    System.out.printf("b: %d - %d\n", edit.getBeginB(), edit.getEndB());
                }
                //System.out.println(entry.toString());
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }   
        
        //new DataCollector().collect();
    }
}
