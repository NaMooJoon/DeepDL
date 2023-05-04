package edu.handong.csee.isel.data.collector;

public class Main {
    public static void main(String[] args) {
        try {
            new DataCollector().collect(args[0], args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
