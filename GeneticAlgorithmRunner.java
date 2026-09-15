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
                    "Use: java -cp out GeneticAlgorithmRunner [path-to-problem.kp | --demo]");
        }

        Path problemFile = null;
        ProblemData problem;
        if (args.length == 1 && args[0].equals("--demo")) {
            problem = new ProblemData("Camera example",
                    new String[]{"Camera", "Speaker", "Console"},
                    new int[]{2, 3, 4}, new int[]{3, 4, 5}, 5);
        } else {
            problemFile = Path.of(args.length == 0
                    ? "benchmarks/benchmarks/p01.kp" : args[0]);
            problem = BenchmarkReader.readProblem(problemFile);
        }

        GeneticAlgorithm algorithm = new GeneticAlgorithm(problem);
        GAResult result = algorithm.solve();

        printInput(problem, problemFile);
        System.out.println("Population size: " + GeneticAlgorithm.DEFAULT_POPULATION_SIZE);
        System.out.println("Generations: " + GeneticAlgorithm.DEFAULT_GENERATIONS);
        System.out.println("Mutation rate: " + GeneticAlgorithm.DEFAULT_MUTATION_RATE);
        System.out.println("Overweight fitness: 0");
        TablePrinter.printGAResult(problem, result);

        if (problemFile != null) {
            Integer knownOptimum = BenchmarkReader.readKnownOptimum(problemFile);
            if (knownOptimum != null) {
                System.out.println("Known optimal value: " + knownOptimum);
                System.out.println("Reached known optimum: "
                        + (result.getFitness() == knownOptimum ? "YES" : "NO"));
            }
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
