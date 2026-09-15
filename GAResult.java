import java.util.Arrays;

// The best valid choice found by one complete GA run.
public class GAResult {
    private final boolean[] genes;
    private final int totalWeight;
    private final int totalValue;
    private final int fitness;
    private final int generations;

    public GAResult(GAChromosome best, int generations) {
        this.genes = best.genesCopy();
        this.totalWeight = best.getTotalWeight();
        this.totalValue = best.getTotalValue();
        this.fitness = best.getFitness();
        this.generations = generations;
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

    public String bits() {
        StringBuilder result = new StringBuilder();
        for (boolean gene : genes) {
            result.append(gene ? '1' : '0');
        }
        return result.toString();
    }
}
