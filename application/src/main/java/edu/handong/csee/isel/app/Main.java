package edu.handong.csee.isel.app;

import org.apache.commons.cli.Options;



import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

public class Main {
   
    public static void main(String[] args) {
        try {
            CommandLine cmd;
            Options options = new Options();
           
            options.addOption(Option.builder("c")
                                    .longOpt("collect")
                                    .hasArgs()
                                    .argName("url")
                                    .argName("key")
                                    .desc("opt1")
                                    .build());
            options.addOption(Option.builder("p")
                                    .longOpt("preprocess")
                                    .hasArgs()
                                    .argName("")
                                    .desc("opt2")
                                    .build());
            options.addOption(Option.builder("tr")
                                    .longOpt("train")
                                    .hasArgs()
                                    .argName("")
                                    .desc("opt1")
                                    .build());
            options.addOption(Option.builder("ts")
                                    .longOpt("test")
                                    .hasArgs()
                                    .argName("")
                                    .desc("opt1")
                                    .build());
            options.addOption(Option.builder("r")
                                    .longOpt("run")
                                    .hasArgs()
                                    .argName("")
                                    .desc("opt1")
                                    .build());

            cmd = new DefaultParser(true).parse(options, args);
            
            if (cmd.hasOption("c")) {
                String[] optionValues = cmd.getOptionValues("c");
                
                new DeepDL().collect(optionValues[0], optionValues[1]);
            } else if (cmd.hasOption("r")) {
                String[] optionValues = cmd.getOptionValues("r");
                
                new DeepDL().run(optionValues[0], optionValues[1], optionValues[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
