import java.io.IOException;
import java.nio.file.Path;

// Start here to run P01, or pass the path to another benchmark file.
public class KnapsackDP {
    public static void main(String[] args) {
        try {
            if (args.length > 1) {
                throw new IllegalArgumentException(
                        "Use: java -cp out KnapsackDP [path-to-problem.kp | --demo]");
            }
            if (args.length == 1 && args[0].equals("--demo")) {
                runDemo();
                return;
            }

            Path problemFile = Path.of(args.length == 0
                    ? "benchmarks/benchmarks/p01.kp" : args[0]);
            ProblemData problem = BenchmarkReader.readProblem(problemFile);
            int[][] dp = DPSolver.buildTable(problem);
            boolean[] selectedItems = DPSolver.findSelectedItems(
                    dp, problem.weights, problem.capacity);
            int bestValue = dp[problem.weights.length][problem.capacity];

            TablePrinter.printProblem(problem, problemFile);
            TablePrinter.printTable(dp, problem.itemNames, problem.capacity, false);
            TablePrinter.printResult(problem, bestValue, selectedItems);

            // Check the answer key only after calculating our own answer.
            Integer knownOptimum = BenchmarkReader.readKnownOptimum(problemFile);
            if (knownOptimum == null) {
                System.out.println("No matching entry in KNOWN_OPTIMA.txt; answer not compared.");
            } else {
                System.out.println("Known optimal value: " + knownOptimum);
                if (bestValue != knownOptimum) {
                    throw new IllegalStateException("The result differs from KNOWN_OPTIMA.txt.");
                }
                System.out.println("Known-optimum check: PASS");
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException error) {
            System.err.println("Could not run knapsack: " + error.getMessage());
            System.exit(1);
        }
    }


    private static void runDemo() {
        ProblemData problem = new ProblemData("Camera example",
                new String[]{"Camera", "Speaker", "Console"},
                new int[]{2, 3, 4},
                new int[]{3, 4, 5},
                5);

        int[][] dp = DPSolver.buildTable(problem);
        boolean[] selectedItems = DPSolver.findSelectedItems(
                dp, problem.weights, problem.capacity);
        int bestValue = dp[problem.weights.length][problem.capacity];

        TablePrinter.printProblem(problem, null);
        TablePrinter.printTable(dp, problem.itemNames, problem.capacity, true);
        TablePrinter.printResult(problem, bestValue, selectedItems);
    }
}
