package knapsack;

import java.io.IOException;
import java.nio.file.Path;

// Run one complete GA solution. P01 is the default problem.
public class GeneticAlgorithmRunner {
    public static void main(String[] args) {
        try {
            run(args);
        } catch (IOException | IllegalArgumentException | IllegalStateException error) {
            System.err.println("Could not run genetic algorithm: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Use: java -cp out knapsack.GeneticAlgorithmRunner [path-to-problem.kp]");
        }

        Path problemFile = Path.of(args.length == 0
                ? "benchmarks/benchmarks/p01.kp" : args[0]);
        ProblemData problem = BenchmarkReader.readProblem(problemFile);

        GeneticAlgorithm algorithm = new GeneticAlgorithm(problem);
        GAResult result = algorithm.solve();

        printInput(problem, problemFile);
        System.out.println("Population size: " + GeneticAlgorithm.DEFAULT_POPULATION_SIZE);
        System.out.println("Generations: " + GeneticAlgorithm.DEFAULT_GENERATIONS);
        System.out.println("Mutation rate: " + GeneticAlgorithm.DEFAULT_MUTATION_RATE);
        System.out.println("Overweight fitness: 0");
        TablePrinter.printGAResult(problem, result);

        Integer knownOptimum = BenchmarkReader.readKnownOptimum(problemFile);
        if (knownOptimum != null) {
            System.out.println("Known optimal value: " + knownOptimum);
            System.out.println("Reached known optimum: "
                    + (result.getFitness() == knownOptimum ? "YES" : "NO"));
        }
    }

    private static void printInput(ProblemData problem, Path problemFile) {
        System.out.println("Problem: " + problem.name);
        if (problemFile != null) {
            System.out.println("Input file: " + problemFile);
        }
        System.out.println("Items: " + problem.weights.length);
        System.out.println("Weight limit: " + problem.capacity);
    }
}

