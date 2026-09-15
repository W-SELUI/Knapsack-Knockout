import java.util.Random;

// Select parents with probability based on their fitness.
public class GASelector {
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
