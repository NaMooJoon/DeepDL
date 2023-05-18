package edu.handong.csee.isel.preprocessor;
import java.io.IOException;
import java.util.logging.Logger;

import edu.handong.csee.isel.preprocessor.tokenizer.BPE;
import edu.handong.csee.isel.preprocessor.tokenizer.Tokenizer;
import edu.handong.csee.isel.preprocessor.data.*;
import edu.handong.csee.isel.preprocessor.data.Dataset.TokenizerMode;

/*
* 하현이가 java file이 모여있는 것을 나에게 건네주는 것으로 하기로 했음.
* 우선 첫번째로 진행해야하는 것은, 이를 하나의 데이터로 합쳐서 python에서 하나의 변수에다가 담고,
* 이를 바탕으로 encoder를 하나 만듦.
* line block(5줄로 구성)을 만들기.
* 세번째로, BPE dictionary를 활용해서 각 line block을 BPE token으로 변환하기.
* */

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    static boolean bpe = false;
    static boolean train = true;
    static boolean test = true;
    public static void main(String[] args) throws IOException, InterruptedException {

        String resources = "./src/main/resources/";

        if (bpe) {

            // Converter lineblock = new Lineblock(resources);
            Converter corpus = new Corpus(resources);
            // lineblock.make();
            corpus.make();

            logger.info("successfully make lineblocks and corpus");
    
            Tokenizer bpe = new BPE(resources);
            bpe.makeDictionary();
            logger.info("successfully make BPE model");

        }
        if (train) {
                System.out.println("BPE Tokenizer => train data set");
                Tokenizer bpe = new BPE(resources);
                Converter trainSetToTokenCSV = new Dataset(resources, bpe, TokenizerMode.TRAINING);
                trainSetToTokenCSV.make();

        }
        if (test) {
            System.out.println("BPE Tokenizer => test data set");
            Tokenizer bpe = new BPE(resources);
            Converter testSetToKenCSV = new Dataset(resources, bpe, TokenizerMode.TESTING);
            testSetToKenCSV.make();

        }

    }
    
}
