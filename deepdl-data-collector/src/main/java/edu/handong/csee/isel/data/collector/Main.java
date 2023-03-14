package edu.handong.csee.isel.data.collector;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.handong.csee.isel.data.collector.util.Utils;

public class Main {
    public static void main(String[] args) {    
       new DataCollector().collect();
    }
}
