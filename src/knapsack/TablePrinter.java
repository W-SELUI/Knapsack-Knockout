package knapsack;

import java.nio.file.Path;

// All console formatting lives here, including the bordered DP table.
public class TablePrinter {
    public static void printProblem(ProblemData problem, Path problemFile) {
        System.out.println("Problem: " + problem.name);
        if (problemFile != null) {
            System.out.println("Input file: " + problemFile);
        }
        System.out.println("Items: " + problem.weights.length);
        System.out.println("Weight limit: " + problem.capacity);
        for (int i = 0; i < problem.weights.length; i++) {
            System.out.println(problem.itemNames[i] + ": weight " + problem.weights[i]
                    + ", value " + problem.values[i]);
        }
        System.out.println();
        System.out.println("DP table: columns are weight limits; cells are best values.");
    }

    public static void printResult(ProblemData problem, int bestValue, boolean[] selectedItems) {
        System.out.println("Best value for the full bag: " + bestValue);
        System.out.println();
        System.out.println("Selected items:");
        int totalWeight = 0;
        int totalValue = 0;
        int selectedCount = 0;

        for (int i = 0; i < problem.weights.length; i++) {
            if (selectedItems[i]) {
                System.out.println("- " + problem.itemNames[i] + ": weight " + problem.weights[i]
                        + ", value " + problem.values[i]);
                totalWeight += problem.weights[i];
                totalValue += problem.values[i];
                selectedCount++;
            }
        }

        if (selectedCount == 0) {
            System.out.println("(none)");
        }
        System.out.println("Total weight: " + totalWeight + " / " + problem.capacity);
        System.out.println("Total value: " + totalValue);

        if (totalWeight > problem.capacity || totalValue != bestValue) {
            throw new IllegalStateException("The selected items do not match the DP answer.");
        }
    }

    public static void printGAResult(ProblemData problem, GAResult result) {
        System.out.println("Generations completed: " + result.getGenerations());
        System.out.println("Best chromosome: " + result.bits());
        System.out.println("Best fitness: " + result.getFitness());
        System.out.println("Selected items:");

        int selectedCount = 0;
        for (int i = 0; i < problem.weights.length; i++) {
            if (result.isSelected(i)) {
                System.out.println("- " + problem.itemNames[i] + ": weight " + problem.weights[i]
                        + ", value " + problem.values[i]);
                selectedCount++;
            }
        }
        if (selectedCount == 0) {
            System.out.println("(none)");
        }
        System.out.println("Total weight: " + result.getTotalWeight()
                + " / " + problem.capacity);
        System.out.println("Total value: " + result.getTotalValue());

        if (result.getTotalWeight() > problem.capacity) {
            throw new IllegalStateException("The reported GA result is overweight.");
        }
    }

    // Print every column, in groups of ten, with borders around all cells.
    public static void printTable(int[][] dp, int capacity) {
        String[] rowLabels = new String[dp.length];
        rowLabels[0] = "No items";
        int labelWidth = "Items available".length();

        for (int i = 1; i < dp.length; i++) {
            rowLabels[i] = "First " + i + (i == 1 ? " item" : " items");
            labelWidth = Math.max(labelWidth, rowLabels[i].length());
        }

        int cellWidth = Math.max(3, Integer.toString(capacity).length());
        for (int[] row : dp) {
            for (int value : row) {
                cellWidth = Math.max(cellWidth, Integer.toString(value).length());
            }
        }

        String labelFormat = "| %-" + labelWidth + "s |";
        String cellFormat = " %" + cellWidth + "s |";

        for (int start = 0; start <= capacity; start += 10) {
            int end = Math.min(start + 9, capacity);
            String border = "+" + "-".repeat(labelWidth + 2) + "+"
                    + ("-".repeat(cellWidth + 2) + "+").repeat(end - start + 1);

            System.out.println();
            System.out.println("Weight limits " + start + " to " + end);
            System.out.println(border);
            System.out.printf(labelFormat, "Items available");
            for (int w = start; w <= end; w++) {
                System.out.printf(cellFormat, w);
            }
            System.out.println();
            System.out.println(border);

            for (int i = 0; i < dp.length; i++) {
                System.out.printf(labelFormat, rowLabels[i]);
                for (int w = start; w <= end; w++) {
                    System.out.printf(cellFormat, dp[i][w]);
                }
                System.out.println();
                System.out.println(border);
            }
        }
    }

}

