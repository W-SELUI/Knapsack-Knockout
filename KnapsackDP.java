public class KnapsackDP {
    public static void main(String[] args) {
        // Matching positions describe the same item in all three arrays.
        String[] itemNames = {"Camera", "Speaker", "Console"};
        int[] weights = {2, 3, 4};
        int[] values = {3, 4, 5};
        int capacity = 5;

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

        System.out.println("Bag capacity: " + capacity + " kg");
        for (int i = 0; i < itemCount; i++) {
            System.out.println(itemNames[i] + ": " + weights[i]
                    + " kg, value $" + values[i]);
        }
        System.out.println();
        System.out.println("DP table - each cell shows the best total value ($)");
        printTable(dp, itemNames, capacity);
        System.out.println("Best value for the full bag: $" + dp[itemCount][capacity]);

        // Work backwards through the table to find a choice of items
        // that achieves the best value.
        boolean[] selectedItems = findSelectedItems(dp, weights, capacity);

        System.out.println();
        System.out.println("Selected items:");
        int totalWeight = 0;
        int totalValue = 0;
        int selectedCount = 0;

        for (int i = 0; i < itemCount; i++) {
            if (selectedItems[i]) {
                System.out.println("- " + itemNames[i] + ": " + weights[i]
                        + " kg, value $" + values[i]);
                totalWeight += weights[i];
                totalValue += values[i];
                selectedCount++;
            }
        }

        if (selectedCount == 0) {
            System.out.println("(none)");
        }
        System.out.println("Total weight: " + totalWeight + " / " + capacity + " kg");
        System.out.println("Total value: $" + totalValue);
    }

    // An unchanged value means we can leave this item out and still
    // achieve the best value. On a tie, we leave the item out.
    private static boolean[] findSelectedItems(int[][] dp, int[] weights, int capacity) {
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

    // Display the saved answers with a border around every row and column.
    private static void printTable(int[][] dp, String[] itemNames, int capacity) {
        String[] rowLabels = new String[dp.length];
        rowLabels[0] = "No items";

        String availableItems = "";
        int labelWidth = "Items available".length();
        for (int i = 1; i < dp.length; i++) {
            if (i > 1) {
                availableItems += ", ";
            }
            availableItems += itemNames[i - 1];
            rowLabels[i] = availableItems;
            labelWidth = Math.max(labelWidth, availableItems.length());
        }

        int cellWidth = (capacity + " kg").length();
        for (int[] row : dp) {
            for (int value : row) {
                cellWidth = Math.max(cellWidth, Integer.toString(value).length());
            }
        }

        String border = "+" + "-".repeat(labelWidth + 2) + "+";
        for (int w = 0; w <= capacity; w++) {
            border += "-".repeat(cellWidth + 2) + "+";
        }

        String labelFormat = "| %-" + labelWidth + "s |";
        String cellFormat = " %" + cellWidth + "s |";

        System.out.println(border);
        System.out.printf(labelFormat, "Items available");
        for (int w = 0; w <= capacity; w++) {
            System.out.printf(cellFormat, w + " kg");
        }
        System.out.println();
        System.out.println(border);

        for (int i = 0; i < dp.length; i++) {
            System.out.printf(labelFormat, rowLabels[i]);
            for (int w = 0; w <= capacity; w++) {
                System.out.printf(cellFormat, dp[i][w]);
            }
            System.out.println();
            System.out.println(border);
        }
    }
}

