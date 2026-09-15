import java.util.Arrays;
import java.util.Random;

// One possible choice of items in the genetic algorithm.
public class GAChromosome {
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
