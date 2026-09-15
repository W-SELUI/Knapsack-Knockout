// Combine two parent chromosomes at one split point.
public class GACrossover {
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
}
