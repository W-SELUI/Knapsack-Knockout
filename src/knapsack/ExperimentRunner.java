package knapsack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Random;

// Run the Q3 statistical experiment for all eight supplied benchmarks.
public class ExperimentRunner {
    private static final String DEFAULT_DIRECTORY = "benchmarks/benchmarks";
    private static final String RESULTS_DIRECTORY = "results";
    private static final int GA_RUNS = 30;
    private static final long BASE_SEED = 2142026L;
    private static final String[] BENCHMARK_NAMES = {
            "p01.kp", "p02.kp", "p03.kp", "p04.kp",
            "p05.kp", "p06.kp", "p07.kp", "p08.kp"
    };

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IOException | IllegalArgumentException | IllegalStateException error) {
            System.err.println("Could not run Q3 experiment: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Use: java -cp out knapsack.ExperimentRunner [benchmark-directory]");
        }

        Path benchmarkDirectory = Path.of(
                args.length == 0 ? DEFAULT_DIRECTORY : args[0]);
        Path resultsDirectory = Path.of(RESULTS_DIRECTORY);
        Files.createDirectories(resultsDirectory);

        Path rawResults = resultsDirectory.resolve("q3_run_results.csv");
        Path summaryResults = resultsDirectory.resolve("q3_summary.csv");

        try (BufferedWriter rawWriter = openCsv(rawResults);
             BufferedWriter summaryWriter = openCsv(summaryResults)) {
            rawWriter.write("problem,run,seed,knownOptimal,gaProfit,gaWeight,gaValid,reachedOptimum,gaNfc,gaTimeMs");
            rawWriter.newLine();
            summaryWriter.write("problem,capacity,knownOptimal,dpProfit,dpWeight,dpValid,dpNfc,dpTimeMs,gaRuns,gaValidRuns,gaSuccesses,gaSuccessRate,gaBestProfit,gaMeanProfit,gaWorstProfit,gaMeanNfc,gaMeanTimeMs");
            summaryWriter.newLine();

            System.out.println("Q3 statistical experiment");
            System.out.println("Problems: P01-P08");
            System.out.println("GA runs per problem: " + GA_RUNS);
            System.out.println("GA settings: population 50, generations 200, mutation rate 0.05");
            System.out.println("GA NFC: chromosome fitness evaluations");
            System.out.println("DP NFC: DP table-cell calculations");
            System.out.println();
            printHeader();
            printBorder();

            for (int problemIndex = 0; problemIndex < BENCHMARK_NAMES.length; problemIndex++) {
                Path problemFile = benchmarkDirectory.resolve(BENCHMARK_NAMES[problemIndex]);
                ProblemData problem = BenchmarkReader.readProblem(problemFile);
                Integer knownOptimum = BenchmarkReader.readKnownOptimum(problemFile);
                if (knownOptimum == null) {
                    throw new IllegalStateException(
                            "No known optimum was found for " + problemFile.getFileName());
                }

                long dpStart = System.nanoTime();
                DPSolver.CompactResult dpResult = DPSolver.solveCompact(problem);
                double dpTimeMs = elapsedMilliseconds(dpStart);
                boolean dpValid = dpResult.getTotalWeight() <= problem.capacity
                        && dpResult.getBestValue() == knownOptimum;

                int bestProfit = Integer.MIN_VALUE;
                int worstProfit = Integer.MAX_VALUE;
                long totalProfit = 0;
                long totalNfc = 0;
                double totalTimeMs = 0.0;
                int validRuns = 0;
                int successes = 0;

                for (int run = 1; run <= GA_RUNS; run++) {
                    long seed = BASE_SEED + (long) problemIndex * GA_RUNS + run;
                    long gaStart = System.nanoTime();
                    GAResult gaResult = new GeneticAlgorithm(
                            problem,
                            GeneticAlgorithm.DEFAULT_POPULATION_SIZE,
                            GeneticAlgorithm.DEFAULT_GENERATIONS,
                            GeneticAlgorithm.DEFAULT_MUTATION_RATE,
                            new Random(seed)).solve();
                    double gaTimeMs = elapsedMilliseconds(gaStart);

                    int gaProfit = gaResult.getFitness();
                    boolean gaValid = gaResult.getTotalWeight() <= problem.capacity;
                    boolean reachedOptimum = gaValid && gaProfit == knownOptimum;

                    bestProfit = Math.max(bestProfit, gaProfit);
                    worstProfit = Math.min(worstProfit, gaProfit);
                    totalProfit += gaProfit;
                    totalNfc += gaResult.getFitnessEvaluations();
                    totalTimeMs += gaTimeMs;
                    if (gaValid) {
                        validRuns++;
                    }
                    if (reachedOptimum) {
                        successes++;
                    }

                    writeRawRow(rawWriter, problemFile, run, seed, knownOptimum,
                            gaResult, gaValid, reachedOptimum, gaTimeMs);
                }

                double meanProfit = totalProfit / (double) GA_RUNS;
                double successRate = successes * 100.0 / GA_RUNS;
                double meanNfc = totalNfc / (double) GA_RUNS;
                double meanTimeMs = totalTimeMs / GA_RUNS;

                writeSummaryRow(summaryWriter, problemFile, problem, knownOptimum,
                        dpResult, dpValid, dpTimeMs, validRuns, successes,
                        successRate, bestProfit, meanProfit, worstProfit,
                        meanNfc, meanTimeMs);
                printSummaryRow(problemFile, knownOptimum, dpResult, dpValid,
                        dpTimeMs, successes, successRate, bestProfit, meanProfit,
                        worstProfit, meanNfc);
            }

            printBorder();
            System.out.println("Raw run data saved to: " + rawResults);
            System.out.println("Summary data saved to: " + summaryResults);
        }
    }

    private static BufferedWriter openCsv(Path path) throws IOException {
        return Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    private static void writeRawRow(BufferedWriter writer, Path problemFile, int run,
            long seed, int knownOptimum, GAResult result, boolean valid,
            boolean reachedOptimum, double timeMs) throws IOException {
        writer.write(String.format(Locale.ROOT,
                "%s,%d,%d,%d,%d,%d,%s,%s,%d,%.3f",
                problemFile.getFileName(), run, seed, knownOptimum,
                result.getFitness(), result.getTotalWeight(), valid,
                reachedOptimum, result.getFitnessEvaluations(), timeMs));
        writer.newLine();
    }

    private static void writeSummaryRow(BufferedWriter writer, Path problemFile,
            ProblemData problem, int knownOptimum, DPSolver.CompactResult dpResult,
            boolean dpValid, double dpTimeMs, int validRuns, int successes,
            double successRate, int bestProfit, double meanProfit, int worstProfit,
            double meanNfc, double meanTimeMs) throws IOException {
        writer.write(String.format(Locale.ROOT,
                "%s,%d,%d,%d,%d,%s,%d,%.3f,%d,%d,%d,%.2f,%d,%.2f,%d,%.2f,%.3f",
                problemFile.getFileName(), problem.capacity, knownOptimum,
                dpResult.getBestValue(), dpResult.getTotalWeight(), dpValid,
                dpResult.getNfc(), dpTimeMs, GA_RUNS, validRuns, successes,
                successRate, bestProfit, meanProfit, worstProfit, meanNfc,
                meanTimeMs));
        writer.newLine();
    }

    private static void printSummaryRow(Path problemFile, int knownOptimum,
            DPSolver.CompactResult dpResult, boolean dpValid, double dpTimeMs,
            int successes, double successRate, int bestProfit, double meanProfit,
            int worstProfit, double meanNfc) {
        System.out.printf(Locale.ROOT,
                "| %-8s | %10d | %8s | %10d | %9.2f | %10d | %12.2f | %10d | %9.1f%% | %12.2f |%n",
                problemFile.getFileName(), knownOptimum,
                dpValid ? "YES" : "NO", dpResult.getBestValue(), dpTimeMs,
                bestProfit, meanProfit, worstProfit, successRate, meanNfc);
    }

    private static double elapsedMilliseconds(long start) {
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private static void printHeader() {
        System.out.println("| Problem  | Known opt. | DP valid | DP profit  | DP ms     | GA best    | GA mean      | GA worst   | Success    | Mean GA NFC  |");
    }

    private static void printBorder() {
        System.out.println("+----------+------------+----------+------------+-----------+------------+--------------+------------+------------+--------------+");
    }
}

