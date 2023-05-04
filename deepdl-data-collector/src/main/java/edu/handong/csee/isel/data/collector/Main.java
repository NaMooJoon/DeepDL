package edu.handong.csee.isel.data.collector;

public class Main {
    public static void main(String[] args) {
        try {
            new DataCollector().collect(args[1], args[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
