// Test chromosome evaluation with the small camera example.
public class GADemo {
    public static void main(String[] args) {
        ProblemData problem = new ProblemData("Camera example",
                new String[]{"Camera", "Speaker", "Console"},
                new int[]{2, 3, 4},
                new int[]{3, 4, 5},
                5);

        GAChromosome[] population = {
                new GAChromosome(new boolean[]{true, true, false}),
                new GAChromosome(new boolean[]{true, false, false}),
                new GAChromosome(new boolean[]{false, true, false}),
                new GAChromosome(new boolean[]{false, true, true})
        };

        System.out.println("Initial population:");
        for (GAChromosome chromosome : population) {
            chromosome.evaluate(problem);
            System.out.println(chromosome.bits()
                    + " | weight: " + chromosome.getTotalWeight()
                    + " | value: " + chromosome.getTotalValue()
                    + " | fitness: " + chromosome.getFitness());
        }

        System.out.println();
        System.out.println("Selected parents:");
        java.util.Random random = new java.util.Random(7);
        for (int i = 0; i < 3; i++) {
            GAChromosome parent = GASelector.chooseParent(population, random);
            System.out.println("Parent " + (i + 1) + ": " + parent.bits());
        }
    }
}
