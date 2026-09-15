import java.util.Random;

// Flip each bit with a small probability, then return the changed child.
public class GAMutation {
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
