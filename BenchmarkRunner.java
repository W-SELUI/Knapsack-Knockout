import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

// Run the Q2 benchmark check for P01 through P08 in one launch.
public class BenchmarkRunner {
    private static final String DEFAULT_DIRECTORY = "benchmarks/benchmarks";
    private static final String[] BENCHMARK_NAMES = {
            "p01.kp", "p02.kp", "p03.kp", "p04.kp",
            "p05.kp", "p06.kp", "p07.kp", "p08.kp"
    };

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IOException | IllegalArgumentException error) {
            System.err.println("Could not run benchmarks: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Use: java -cp out BenchmarkRunner [benchmark-directory]");
        }

        Path benchmarkDirectory = Path.of(
                args.length == 0 ? DEFAULT_DIRECTORY : args[0]);

        System.out.println("Q2 benchmark check: DP and GA on P01-P08");
        System.out.println("Benchmark directory: " + benchmarkDirectory);
        System.out.println("Each GA result below is one random run.");
        System.out.println("GA settings: population 50, generations 200, mutation rate 0.05");
        System.out.println();

        printHeader();
        printBorder();

        for (String benchmarkName : BENCHMARK_NAMES) {
            runOneBenchmark(benchmarkDirectory.resolve(benchmarkName));
        }

        printBorder();
        System.out.println("The DP summary uses a compact table so large capacities do not create a huge output table.");
    }

    private static void runOneBenchmark(Path problemFile) throws IOException {
        if (!Files.exists(problemFile)) {
            System.out.printf(Locale.ROOT, "%-10s %-10s%n",
                    problemFile.getFileName(), "FILE NOT FOUND");
            return;
        }

        ProblemData problem = BenchmarkReader.readProblem(problemFile);
        Integer knownOptimum = BenchmarkReader.readKnownOptimum(problemFile);

        long dpStart = System.nanoTime();
        DPSolver.CompactResult dpResult = DPSolver.solveCompact(problem);
        double dpMilliseconds = elapsedMilliseconds(dpStart);

        long gaStart = System.nanoTime();
        GAResult gaResult = new GeneticAlgorithm(problem).solve();
        double gaMilliseconds = elapsedMilliseconds(gaStart);
        boolean gaValid = gaResult.getTotalWeight() <= problem.capacity;

        System.out.printf(Locale.ROOT,
                "| %-7s | %10d | %10s | %9d | %9d | %9d | %9d | %10f | %10f | %-9s |%n",
                problemFile.getFileName(),
                problem.capacity,
                knownOptimum == null ? "?" : knownOptimum,
                dpResult.getBestValue(),
                dpResult.getTotalWeight(),
                gaResult.getFitness(),
                gaResult.getTotalWeight(),
                dpMilliseconds,
                gaMilliseconds,
                gaValid ? "YES" : "NO");
    }

    private static double elapsedMilliseconds(long start) {
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private static void printHeader() {
        System.out.println("| Problem |   Capacity |      Known | DP profit | DP weight | GA profit | GA weight |    DP ms   |   GA ms    | GA valid  |");
    }

    private static void printBorder() {
        System.out.println("+---------+------------+------------+-----------+-----------+-----------+-----------+------------+------------+-----------+");
    }
}
