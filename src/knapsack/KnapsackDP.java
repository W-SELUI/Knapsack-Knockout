package knapsack;

import java.io.IOException;
import java.nio.file.Path;

// Start here to run P01, or pass the path to another benchmark file.
public class KnapsackDP {
    public static void main(String[] args) {
        try {
            if (args.length > 1) {
                throw new IllegalArgumentException(
                        "Use: java -cp out knapsack.KnapsackDP [path-to-problem.kp]");
            }

            Path problemFile = Path.of(args.length == 0
                    ? "benchmarks/benchmarks/p01.kp" : args[0]);
            ProblemData problem = BenchmarkReader.readProblem(problemFile);
            int[][] dp = DPSolver.buildTable(problem);
            boolean[] selectedItems = DPSolver.findSelectedItems(
                    dp, problem.weights, problem.capacity);
            int bestValue = dp[problem.weights.length][problem.capacity];

            TablePrinter.printProblem(problem, problemFile);
            TablePrinter.printTable(dp, problem.capacity);
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
}

