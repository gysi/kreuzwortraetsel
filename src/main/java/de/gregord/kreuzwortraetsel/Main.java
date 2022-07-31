package de.gregord.kreuzwortraetsel;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Main {
    public static final Logger LOG = LoggerFactory.getLogger(Main.class);
    public static Settings settings;

    public static void main(String[] args) throws IOException {
        LOG.info("starting");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        String path = "src/main/resources/settings.yaml";
        if(args.length > 0){
            path = args[0];
        }
        settings = objectMapper.readValue(new File(path), Settings.class);
        List<String> wordList = settings.getWordList().stream().map(String::toUpperCase).toList();
        List<String> optionalWordList = settings.getOptionalWordList().stream().map(String::toUpperCase).toList();
        LOG.info("wordcount: " + (wordList.size()+optionalWordList.size()));

        Statistics statistics = new Statistics(wordList, optionalWordList, settings);
        PuzzleResults puzzleResults = new PuzzleResults(wordList.size(), optionalWordList.size());
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < settings.getThreadCount(); i++) {
            Thread thread = new Thread(new PuzzleSolverRunnable(
                    puzzleResults,
                    wordList,
                    optionalWordList,
                    settings.getFieldWidth(),
                    settings.getFieldHeight(),
                    settings.getBlockedArea(),
                    statistics
            ));
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("words used: " + (puzzleResults.getMaxIterations().get() + 1) + " of "
                + (puzzleResults.getWordListSize()+puzzleResults.getOptionalWordListSize()));
        System.out.println("important words used: "
                + (puzzleResults.getWordListSize() - puzzleResults.getMissingWordsCount().get())
                + " of " + puzzleResults.getWordListSize());
        System.out.println("optional words used: "
                + (puzzleResults.getOptionalWordListSize() - puzzleResults.getMissingOptionalWordsCount().get())
                + " of " + puzzleResults.getOptionalWordListSize());
        for (BestPuzzleResult solvedPuzzle : puzzleResults.getSolvedPuzzles()) {
            System.out.println(solvedPuzzle.solvedPuzzle());
            System.out.println("Missing Words: ");
            for (String missingWord : solvedPuzzle.missingWords()) {
                System.out.println(missingWord);
            }
            System.out.println("\nMissing optional Words: ");
            for (String missingWord : solvedPuzzle.missingOptionalWords()) {
                System.out.println(missingWord);
            }
        }

//        statistics.print();

//        System.out.println(high);
//        for (PlacedWordInfo placedWord : placedWords) {
//            System.out.println(placedWord);
//        }
    }

    public static record BestPuzzleResult(String solvedPuzzle, List<String> missingWords,
                                          List<String> missingOptionalWords, int iterations) {}

    public static record PuzzleResult(Field solvedPuzzle, List<String> missingWords,
                                          List<String> missingOptionalWords, int iterations) {}

    public static class PuzzleResults {
        private final int wordListSize;
        private final int optionalWordListSize;
        private List<BestPuzzleResult> solvedPuzzles = new ArrayList<>();
        private final AtomicInteger maxIterations = new AtomicInteger(0);
        private final AtomicInteger missingWordsCount;
        private final AtomicInteger missingOptionalWordsCount;
        private static final Object lock = new Object();

        public PuzzleResults(int wordListSize, int optionalWordListSize){
            this.wordListSize = wordListSize;
            this.optionalWordListSize = optionalWordListSize;
            this.missingWordsCount = new AtomicInteger(wordListSize);
            this.missingOptionalWordsCount = new AtomicInteger(optionalWordListSize);
        }

        public void addSolution(PuzzleResult puzzleResult){
            if(isItABetterSolution(puzzleResult)){
                synchronized (lock) {
                    if(isItABetterSolution(puzzleResult)) {
                        saveBetterSolution(puzzleResult);
                    }
                }
            }else if (isItTheSameSolution(puzzleResult)){
                synchronized (lock){
                    if(isItTheSameSolution(puzzleResult)) {
                        if (solvedPuzzles.size() < 8) {
                            solvedPuzzles.add(
                                    new BestPuzzleResult(
                                            puzzleResult.solvedPuzzle.toString(),
                                            puzzleResult.missingWords,
                                            puzzleResult.missingOptionalWords,
                                            puzzleResult.iterations
                            ));
                        }
                    }
                }
            }
        }

        private void saveBetterSolution(PuzzleResult puzzleResult){
            LOG.info("new High!: " + (puzzleResult.iterations() + 1) + " Words used. "
                    + (this.getWordListSize() - puzzleResult.missingWords().size()) + " Important Words. "
                    + (this.getOptionalWordListSize() - puzzleResult.missingOptionalWords().size()) + " Optional Words. ");
            this.maxIterations.set(puzzleResult.iterations());
            this.missingWordsCount.set(puzzleResult.missingWords().size());
            this.missingOptionalWordsCount.set(puzzleResult.missingOptionalWords.size());
            solvedPuzzles = new ArrayList<>();
            solvedPuzzles.add(
                    new BestPuzzleResult(
                        puzzleResult.solvedPuzzle.toString(),
                        puzzleResult.missingWords,
                        puzzleResult.missingOptionalWords,
                        puzzleResult.iterations
            ));
        }

        private boolean isItTheSameSolution(PuzzleResult puzzleResult){
            if(puzzleResult.missingWords().size() == missingWordsCount.get()
                    && puzzleResult.iterations() == this.maxIterations.get()) {
                return true;
            }
            return false;
        }

        private boolean isItABetterSolution(PuzzleResult puzzleResult){
            if (puzzleResult.missingWords().size() < missingWordsCount.get()
                    || (puzzleResult.missingWords().size() == missingWordsCount.get()
                            && puzzleResult.iterations() > this.maxIterations.get())){
                return true;
            }
            return false;
        }

        public AtomicInteger getMaxIterations(){
            return maxIterations;
        }

        public List<BestPuzzleResult> getSolvedPuzzles(){
            return this.solvedPuzzles;
        }

        public int getWordListSize() {
            return wordListSize;
        }

        public int getOptionalWordListSize() {
            return optionalWordListSize;
        }

        public AtomicInteger getMissingWordsCount() {
            return missingWordsCount;
        }

        public AtomicInteger getMissingOptionalWordsCount() {
            return missingOptionalWordsCount;
        }
    }

    public static class PuzzleSolverRunnable implements Runnable {

        private final List<String> wordList;
        private final List<String> optionalWordList;
        private final int width;
        private final int height;
        private final PuzzleResults puzzleResults;
        private final BlockedArea blockedArea;
        private final Statistics statistics;
        private static final AtomicInteger solveCounter = new AtomicInteger();
        private static Instant solveCountTimeMeasure = Instant.now();
        private static int measures = 0;
        public static double accumilatedTimeInSeconds = 0;

        public static void printSolveCount(){
            int i = solveCounter.incrementAndGet();
            if(i % 100000 == 0){
                String infoLog = ""+i;
                Instant temp = PuzzleSolverRunnable.solveCountTimeMeasure;
                solveCountTimeMeasure = Instant.now();
                if(i >= 25000) {
                    Duration between = Duration.between(temp, solveCountTimeMeasure);
                    measures++;
                    accumilatedTimeInSeconds += (between.toMillis() / 1000.);
//                LOG.info(""+i + " " + between.getSeconds()+"s " + between.toNanosPart()/1000000 + "ms");
                    infoLog += " " + between.toMillis() / 1000. + "s (Avg: " + accumilatedTimeInSeconds / measures + ")";
                }
                LOG.info(infoLog);
            }
        }

        public PuzzleSolverRunnable(PuzzleResults puzzleResults, List<String> wordList, List<String> optionalWordList,
                int width, int height, BlockedArea blockedArea, Statistics statistics){
            this.puzzleResults = puzzleResults;
            this.wordList = wordList;
            this.optionalWordList = optionalWordList;
            this.width = width;
            this.height = height;
            this.blockedArea = blockedArea;
            this.statistics = statistics;
        }

        @Override
        public void run() {
            Solver solver = new Solver(width, height, wordList, optionalWordList, blockedArea);
            for (int i = 0; i < settings.getIterations(); i++) {
                SolvedPuzzleInfo solvedPuzzleInfo = solver.solve(puzzleResults.getMissingWordsCount().get());
                // TODO just for benchmarking
//                SolvedPuzzleInfo solvedPuzzleInfo = solver.solve(wordList.size());

                PuzzleResult puzzleResult = new PuzzleResult(
                        solvedPuzzleInfo.getField(),
                        solvedPuzzleInfo.getMissingWordsList(),
                        solvedPuzzleInfo.getMissingOptionalWordsList(),
                        solvedPuzzleInfo.getIterations()
                );
                puzzleResults.addSolution(puzzleResult);
                if(solvedPuzzleInfo.getIterations() == wordList.size() + optionalWordList.size() - 1){
                    LOG.info("All words got fit!!!");
                    break;
                }
//                statistics.addToStatistics(solvedPuzzleInfo);
                printSolveCount();
            }
        }
    }
}
