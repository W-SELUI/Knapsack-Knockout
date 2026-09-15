import java.util.Random;

// Complete GA solver: population, selection, crossover, mutation and tracking.
public class GeneticAlgorithm {
    public static final int DEFAULT_POPULATION_SIZE = 50;
    public static final int DEFAULT_GENERATIONS = 200;
    public static final double DEFAULT_MUTATION_RATE = 0.05;

    private final ProblemData problem;
    private final int populationSize;
    private final int generations;
    private final double mutationRate;
    private final Random random;

    public GeneticAlgorithm(ProblemData problem) {
        this(problem, DEFAULT_POPULATION_SIZE, DEFAULT_GENERATIONS,
                DEFAULT_MUTATION_RATE, new Random());
    }

    public GeneticAlgorithm(ProblemData problem, int populationSize,
            int generations, double mutationRate, Random random) {
        if (populationSize < 2) {
            throw new IllegalArgumentException("Population size must be at least 2.");
        }
        if (generations < 1) {
            throw new IllegalArgumentException("Generations must be at least 1.");
        }
        if (problem.weights.length == 0) {
            throw new IllegalArgumentException("The GA needs at least one item.");
        }
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("Mutation rate must be between 0 and 1.");
        }
        this.problem = problem;
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.random = random;
    }

    public GAResult solve() {
        GAChromosome[] population = createInitialPopulation();
        GAChromosome best = bestOf(population).copy();

        for (int generation = 0; generation < generations; generation++) {
            GAChromosome[] nextPopulation = new GAChromosome[populationSize];

            // Elitism: keep the best choice so far in the next generation.
            nextPopulation[0] = best.copy();
            int nextIndex = 1;

            while (nextIndex < populationSize) {
                GAChromosome firstParent = GASelector.chooseParent(population, random);
                GAChromosome secondParent = GASelector.chooseParent(population, random);
                GAChromosome[] children = GACrossover.onePointRandom(
                        firstParent, secondParent, random);

                for (GAChromosome child : children) {
                    if (nextIndex >= populationSize) {
                        break;
                    }
                    GAChromosome mutatedChild = GAMutation.mutate(
                            child, mutationRate, random);
                    mutatedChild.evaluate(problem);
                    nextPopulation[nextIndex] = mutatedChild;
                    if (mutatedChild.getFitness() > best.getFitness()) {
                        best = mutatedChild.copy();
                    }
                    nextIndex++;
                }
            }

            population = nextPopulation;
        }

        return new GAResult(best, generations);
    }

    private GAChromosome[] createInitialPopulation() {
        GAChromosome[] population = new GAChromosome[populationSize];
        for (int i = 0; i < populationSize; i++) {
            population[i] = GAChromosome.random(problem.weights.length, random);
            population[i].evaluate(problem);
        }
        return population;
    }

    private static GAChromosome bestOf(GAChromosome[] population) {
        GAChromosome best = population[0];
        for (int i = 1; i < population.length; i++) {
            if (population[i].getFitness() > best.getFitness()) {
                best = population[i];
            }
        }
        return best;
    }
}
