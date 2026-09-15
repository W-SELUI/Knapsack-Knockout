// Shared DP calculation for both benchmark files and the demo.
public class DPSolver {
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
