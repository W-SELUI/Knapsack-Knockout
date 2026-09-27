import java.util.Arrays;
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
    private long fitnessEvaluations;

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
        return solve(null);
    }

    // Run the GA and optionally report the best-so-far profit after each
    // meaningful step to the live graph.
    public GAResult solve(ProgressListener listener) {
        fitnessEvaluations = 0;
        GAChromosome[] population = createInitialPopulation();
        GAChromosome best = bestOf(population).copy();

        if (listener != null) {
            listener.onProgress("GA", fitnessEvaluations, best.getFitness());
        }

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
                    fitnessEvaluations++;
                    nextPopulation[nextIndex] = mutatedChild;
                    if (mutatedChild.getFitness() > best.getFitness()) {
                        best = mutatedChild.copy();
                    }
                    nextIndex++;
                }
            }

            population = nextPopulation;

            if (listener != null) {
                listener.onProgress("GA", fitnessEvaluations, best.getFitness());
            }
        }

        return new GAResult(best, generations, fitnessEvaluations);
    }

    private GAChromosome[] createInitialPopulation() {
        GAChromosome[] population = new GAChromosome[populationSize];
        for (int i = 0; i < populationSize; i++) {
            population[i] = GAChromosome.random(problem.weights.length, random);
            population[i].evaluate(problem);
            fitnessEvaluations++;
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

// ---------------- GAChromosome ----------------
// One possible choice of items in the genetic algorithm.
class GAChromosome {
    private final boolean[] genes;
    private int totalWeight;
    private int totalValue;
    private int fitness;

    public GAChromosome(boolean[] genes) {
        this.genes = Arrays.copyOf(genes, genes.length);
    }

    public static GAChromosome random(int length, Random random) {
        boolean[] genes = new boolean[length];
        for (int i = 0; i < length; i++) {
            genes[i] = random.nextBoolean();
        }
        return new GAChromosome(genes);
    }

    // Calculate this choice's weight, value and fitness.
    // An overweight choice receives fitness 0 as our penalty rule.
    public void evaluate(ProblemData problem) {
        if (genes.length != problem.weights.length) {
            throw new IllegalArgumentException("Chromosome length must match the item count.");
        }
        totalWeight = 0;
        totalValue = 0;

        for (int i = 0; i < genes.length; i++) {
            if (genes[i]) {
                totalWeight += problem.weights[i];
                totalValue += problem.values[i];
            }
        }

        fitness = totalWeight <= problem.capacity ? totalValue : 0;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public int getTotalValue() {
        return totalValue;
    }

    public int getFitness() {
        return fitness;
    }

    public String bits() {
        StringBuilder result = new StringBuilder();
        for (boolean gene : genes) {
            result.append(gene ? '1' : '0');
        }
        return result.toString();
    }

    // Give crossover a copy so it cannot change the parent by accident.
    public boolean[] genesCopy() {
        return Arrays.copyOf(genes, genes.length);
    }

    public GAChromosome copy() {
        GAChromosome copy = new GAChromosome(genes);
        copy.totalWeight = totalWeight;
        copy.totalValue = totalValue;
        copy.fitness = fitness;
        return copy;
    }
}

// ---------------- GASelector ----------------
// Select parents with probability based on their fitness.
class GASelector {
    public static GAChromosome chooseParent(GAChromosome[] population, Random random) {
        int totalFitness = 0;
        for (GAChromosome chromosome : population) {
            totalFitness += chromosome.getFitness();
        }

        // If every choice is invalid, choose one randomly so the algorithm
        // can still continue and try a different generation.
        if (totalFitness == 0) {
            return population[random.nextInt(population.length)];
        }

        // Treat fitness values as tickets. Higher fitness means more tickets.
        int winningTicket = random.nextInt(totalFitness) + 1;
        int ticketsSeen = 0;
        for (GAChromosome chromosome : population) {
            ticketsSeen += chromosome.getFitness();
            if (winningTicket <= ticketsSeen) {
                return chromosome;
            }
        }

        throw new IllegalStateException("Could not select a parent.");
    }
}

// ---------------- GACrossover ----------------
// Combine two parent chromosomes at one split point.
class GACrossover {
    public static GAChromosome[] onePoint(
            GAChromosome firstParent, GAChromosome secondParent, int splitPoint) {
        boolean[] firstGenes = firstParent.genesCopy();
        boolean[] secondGenes = secondParent.genesCopy();

        if (firstGenes.length != secondGenes.length) {
            throw new IllegalArgumentException("Parents must have the same length.");
        }
        if (splitPoint <= 0 || splitPoint >= firstGenes.length) {
            throw new IllegalArgumentException("The split must be inside the chromosome.");
        }

        boolean[] firstChildGenes = new boolean[firstGenes.length];
        boolean[] secondChildGenes = new boolean[firstGenes.length];

        for (int i = 0; i < firstGenes.length; i++) {
            if (i < splitPoint) {
                firstChildGenes[i] = firstGenes[i];
                secondChildGenes[i] = secondGenes[i];
            } else {
                firstChildGenes[i] = secondGenes[i];
                secondChildGenes[i] = firstGenes[i];
            }
        }

        return new GAChromosome[]{
                new GAChromosome(firstChildGenes),
                new GAChromosome(secondChildGenes)
        };
    }

    public static GAChromosome[] onePointRandom(
            GAChromosome firstParent, GAChromosome secondParent, Random random) {
        int length = firstParent.genesCopy().length;
        if (length < 2) {
            return new GAChromosome[]{firstParent.copy(), secondParent.copy()};
        }
        int splitPoint = 1 + random.nextInt(length - 1);
        return onePoint(firstParent, secondParent, splitPoint);
    }
}

// ---------------- GAMutation ----------------
// Flip each bit with a small probability, then return the changed child.
class GAMutation {
    public static GAChromosome mutate(
            GAChromosome original, double mutationRate, Random random) {
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("Mutation rate must be between 0 and 1.");
        }

        boolean[] genes = original.genesCopy();
        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < mutationRate) {
                genes[i] = !genes[i];
            }
        }
        return new GAChromosome(genes);
    }
}

// ---------------- GAResult ----------------
// The best valid choice found by one complete GA run.
class GAResult {
    private final boolean[] genes;
    private final int totalWeight;
    private final int totalValue;
    private final int fitness;
    private final int generations;
    private final long fitnessEvaluations;

    public GAResult(GAChromosome best, int generations, long fitnessEvaluations) {
        this.genes = best.genesCopy();
        this.totalWeight = best.getTotalWeight();
        this.totalValue = best.getTotalValue();
        this.fitness = best.getFitness();
        this.generations = generations;
        this.fitnessEvaluations = fitnessEvaluations;
    }

    public boolean[] genesCopy() {
        return Arrays.copyOf(genes, genes.length);
    }

    public boolean isSelected(int index) {
        return genes[index];
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public int getTotalValue() {
        return totalValue;
    }

    public int getFitness() {
        return fitness;
    }

    public int getGenerations() {
        return generations;
    }

    public long getFitnessEvaluations() {
        return fitnessEvaluations;
    }

    public String bits() {
        StringBuilder result = new StringBuilder();
        for (boolean gene : genes) {
            result.append(gene ? '1' : '0');
        }
        return result.toString();
    }
}
