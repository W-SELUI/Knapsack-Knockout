// Demonstrate one crossover step with a fixed split point.
public class GACrossoverDemo {
    public static void main(String[] args) {
        ProblemData problem = new ProblemData("Camera example",
                new String[]{"Camera", "Speaker", "Console"},
                new int[]{2, 3, 4},
                new int[]{3, 4, 5},
                5);

        GAChromosome firstParent = new GAChromosome(
                new boolean[]{true, true, false});       // 110
        GAChromosome secondParent = new GAChromosome(
                new boolean[]{false, false, true});      // 001

        // Split after the first bit: 1 | 10 and 0 | 01.
        GAChromosome[] children = GACrossover.onePoint(
                firstParent, secondParent, 1);

        System.out.println("Parent 1: " + firstParent.bits());
        System.out.println("Parent 2: " + secondParent.bits());
        System.out.println();
        System.out.println("Children after evaluation:");
        for (int i = 0; i < children.length; i++) {
            children[i].evaluate(problem);
            System.out.println("Child " + (i + 1) + ":  " + children[i].bits()
                    + " | weight: " + children[i].getTotalWeight()
                    + " | value: " + children[i].getTotalValue()
                    + " | fitness: " + children[i].getFitness());
        }
    }
}
