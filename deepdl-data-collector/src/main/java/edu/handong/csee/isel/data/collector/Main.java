package edu.handong.csee.isel.data.collector;

public class Main {
    public static void main(String[] args) {
        /*
        try { 
            
            Git git = Git.open(new File(Utils.getProjectPath(), 
                               String.join(System.getProperty("file.separator"),
                                           "..", ".git")));
            CanonicalTreeParser oldParser = new CanonicalTreeParser();
            
            oldParser.reset(git.getRepository().newObjectReader(),
                            new RevWalk(git.getRepository())
                            .parseCommit(
                                    RevCommit.fromString(
                                            "183b6199d6470aad0e85f645e063126de8a009b2"))
                            .getTree());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<DiffEntry> entries = git.diff()
                                         .setOutputStream(out)
                                         .setOldTree(oldParser)
                                         .setNewTree(
                                                new CanonicalTreeParser(
                                                        null,
                                                        git.getRepository().newObjectReader(),
                                                        new RevWalk(git.getRepository())
                                                                .parseCommit(
                                                                        RevCommit.fromString(
                                                                                "a0eeca7006e52646a0cbc4d94912d1069b59854c"))
                                                                .getTree()))
                                         .call();
            System.out.println(out.toString());

            for (DiffEntry entry : entries) {
                System.out.println(entry.toString());
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }   
        */                      
        //System.out.println(new Timestamp(new Date().getTime()).toString().replaceAll("-|\\s|:|\\.", ""));
        new DataCollector().collect();
    }
}
