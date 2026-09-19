// Shared DP calculation for both benchmark files and the demo.
public class DPSolver {
    // A compact one-dimensional DP calculation for benchmark summaries.
    // It keeps the best value and the weight of one best choice for each
    // capacity, without storing the full table used for the teaching output.
    public static CompactResult solveCompact(ProblemData problem) {
        int[] bestValues = new int[problem.capacity + 1];
        int[] bestWeights = new int[problem.capacity + 1];

        for (int i = 0; i < problem.weights.length; i++) {
            int itemWeight = problem.weights[i];
            int itemValue = problem.values[i];

            // Descending capacities make each item available at most once.
            for (int capacity = problem.capacity; capacity >= itemWeight; capacity--) {
                int candidateValue = itemValue + bestValues[capacity - itemWeight];
                int candidateWeight = itemWeight + bestWeights[capacity - itemWeight];

                if (candidateValue > bestValues[capacity]
                        || (candidateValue == bestValues[capacity]
                        && candidateWeight < bestWeights[capacity])) {
                    bestValues[capacity] = candidateValue;
                    bestWeights[capacity] = candidateWeight;
                }
            }
        }

        return new CompactResult(bestValues[problem.capacity], bestWeights[problem.capacity]);
    }

    public static final class CompactResult {
        private final int bestValue;
        private final int totalWeight;

        private CompactResult(int bestValue, int totalWeight) {
            this.bestValue = bestValue;
            this.totalWeight = totalWeight;
        }

        public int getBestValue() {
            return bestValue;
        }

        public int getTotalWeight() {
            return totalWeight;
        }
    }

    public static int[][] buildTable(ProblemData problem) {
        int[] weights = problem.weights;
        int[] values = problem.values;
        int capacity = problem.capacity;
        int itemCount = weights.length;

        // Row 0 means no items; column 0 means a zero-weight limit.
        // Java starts all cells at 0.
        int[][] dp = new int[itemCount + 1][capacity + 1];

        // Add one available item per row.
        for (int i = 1; i <= itemCount; i++) {
            // Array positions start at 0, so row 1 uses item at position 0.
            int currentWeight = weights[i - 1];
            int currentValue = values[i - 1];

            // Work through each possible bag limit in this row.
            for (int w = 1; w <= capacity; w++) {
                int leaveOut = dp[i - 1][w];

                if (currentWeight > w) {
                    // This item cannot fit: keep the answer from the row above.
                    dp[i][w] = leaveOut;
                } else {
                    // Use the previous row for leftover space so this item
                    // can only be taken once.
                    int remainingSpace = w - currentWeight;
                    int take = currentValue + dp[i - 1][remainingSpace];
                    dp[i][w] = Math.max(leaveOut, take);
                }
            }
        }

        return dp;
    }

    // An unchanged value means we can leave this item out and still
    // achieve the best value. On a tie, we leave the item out.
    public static boolean[] findSelectedItems(int[][] dp, int[] weights, int capacity) {
        boolean[] selectedItems = new boolean[weights.length];
        int remainingCapacity = capacity;

        for (int i = weights.length; i > 0; i--) {
            if (dp[i][remainingCapacity] != dp[i - 1][remainingCapacity]) {
                selectedItems[i - 1] = true;
                remainingCapacity -= weights[i - 1];
            }
        }

        return selectedItems;
    }

}
