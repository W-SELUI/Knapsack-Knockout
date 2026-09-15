import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class KnapsackDP {
    public static void main(String[] args) {
        try {
            run(args);
        } catch (IOException | IllegalArgumentException | IllegalStateException error) {
            System.err.println("Could not run knapsack: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Use: java KnapsackDP.java [path-to-problem.kp | --demo]");
        }

        // No argument loads P01. Use --demo to keep practising with our three items.
        boolean isDemo = args.length == 1 && args[0].equals("--demo");
        Path problemFile = null;
        ProblemData problem;

        if (isDemo) {
            problem = new ProblemData("Camera example",
                    new String[]{"Camera", "Speaker", "Console"},
                    new int[]{2, 3, 4}, new int[]{3, 4, 5}, 5);
        } else {
            problemFile = Path.of(args.length == 0
                    ? "benchmarks/benchmarks/p01.kp" : args[0]);
            problem = readProblem(problemFile);
        }

        String[] itemNames = problem.itemNames;
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

        System.out.println("Problem: " + problem.name);
        if (problemFile != null) {
            System.out.println("Input file: " + problemFile);
        }
        System.out.println("Items: " + itemCount);
        System.out.println("Weight limit: " + capacity);
        for (int i = 0; i < itemCount; i++) {
            System.out.println(itemNames[i] + ": weight " + weights[i]
                    + ", value " + values[i]);
        }

        System.out.println();
        System.out.println("DP table: columns are weight limits; cells are best values.");
        printTable(dp, itemNames, capacity, isDemo);
        int bestValue = dp[itemCount][capacity];
        System.out.println("Best value for the full bag: " + bestValue);

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
                System.out.println("- " + itemNames[i] + ": weight " + weights[i]
                        + ", value " + values[i]);
                totalWeight += weights[i];
                totalValue += values[i];
                selectedCount++;
            }
        }

        if (selectedCount == 0) {
            System.out.println("(none)");
        }
        System.out.println("Total weight: " + totalWeight + " / " + capacity);
        System.out.println("Total value: " + totalValue);

        if (totalWeight > capacity || totalValue != bestValue) {
            throw new IllegalStateException("The selected items do not match the DP answer.");
        }

        // Look up the answer key only after computing our own answer.
        if (problemFile != null) {
            Integer knownOptimum = readKnownOptimum(problemFile);
            if (knownOptimum == null) {
                System.out.println("No matching entry in KNOWN_OPTIMA.txt; answer not compared.");
            } else {
                System.out.println("Known optimal value: " + knownOptimum);
                if (bestValue != knownOptimum) {
                    throw new IllegalStateException("The result differs from KNOWN_OPTIMA.txt.");
                }
                System.out.println("Known-optimum check: PASS");
            }
        }
    }

    // Read the provided .kp format: headers, then index/weight/profit item rows.
    // OPTIMAL metadata is ignored: it is not an input to the DP calculation.
    private static ProblemData readProblem(Path file) throws IOException {
        String name = file.getFileName().toString();
        int dimension = -1;
        int capacity = -1;
        boolean inItemSection = false;
        List<int[]> items = new ArrayList<>();
        Set<Integer> itemIds = new HashSet<>();
        long totalPossibleValue = 0;

        for (String rawLine : Files.readAllLines(file)) {
            String line = rawLine.replace("\uFEFF", "").split("#", 2)[0].trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.equalsIgnoreCase("EOF")) {
                break;
            }
            if (line.equalsIgnoreCase("ITEM_SECTION")) {
                inItemSection = true;
                continue;
            }

            if (inItemSection) {
                String[] fields = line.split("\\s+");
                if (fields.length != 3) {
                    throw new IllegalArgumentException(
                            "Each item needs an index, weight and profit: " + line);
                }
                int id = Integer.parseInt(fields[0]);
                int weight = Integer.parseInt(fields[1]);
                int value = Integer.parseInt(fields[2]);
                if (id <= 0 || weight <= 0 || value < 0 || !itemIds.add(id)) {
                    throw new IllegalArgumentException(
                            "Items need unique positive IDs, positive weights and nonnegative profits.");
                }
                items.add(new int[]{id, weight, value});
                totalPossibleValue += value;
            } else {
                int colon = line.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                String key = line.substring(0, colon).trim().toUpperCase(Locale.ROOT);
                String value = line.substring(colon + 1).trim();
                switch (key) {
                    case "NAME":
                        name = value;
                        break;
                    case "DIMENSION":
                        dimension = Integer.parseInt(value);
                        break;
                    case "CAPACITY":
                        capacity = Integer.parseInt(value);
                        break;
                    default:
                        // Descriptive headers do not change the packing problem.
                        break;
                }
            }
        }

        if (!inItemSection || dimension < 0 || capacity < 0) {
            throw new IllegalArgumentException(
                    "The file needs DIMENSION, CAPACITY and ITEM_SECTION.");
        }
        if (dimension != items.size()) {
            throw new IllegalArgumentException(
                    "DIMENSION says " + dimension + " items, but the file contains " + items.size() + ".");
        }
        if (capacity == Integer.MAX_VALUE || totalPossibleValue > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The input exceeds this version's integer limits.");
        }

        String[] itemNames = new String[dimension];
        int[] weights = new int[dimension];
        int[] values = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            int[] item = items.get(i);
            itemNames[i] = "Item " + item[0];
            weights[i] = item[1];
            values[i] = item[2];
        }
        return new ProblemData(name, itemNames, weights, values, capacity);
    }

    // The answer key is expected beside the selected benchmark file.
    private static Integer readKnownOptimum(Path problemFile) throws IOException {
        Path answerKey = problemFile.resolveSibling("KNOWN_OPTIMA.txt");
        if (!Files.exists(answerKey)) {
            return null;
        }
        for (String line : Files.readAllLines(answerKey)) {
            String[] fields = line.trim().split("\\s+");
            if (fields.length >= 4
                    && fields[0].equalsIgnoreCase(problemFile.getFileName().toString())) {
                return Integer.valueOf(fields[3]);
            }
        }
        return null;
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

    // Print every column, in groups of ten, with borders around all cells.
    private static void printTable(int[][] dp, String[] itemNames, int capacity, boolean isDemo) {
        String[] rowLabels = new String[dp.length];
        rowLabels[0] = "No items";
        int labelWidth = "Items available".length();
        String availableItems = "";

        for (int i = 1; i < dp.length; i++) {
            if (isDemo) {
                availableItems += (i == 1 ? "" : ", ") + itemNames[i - 1];
                rowLabels[i] = availableItems;
            } else {
                rowLabels[i] = "First " + i + (i == 1 ? " item" : " items");
            }
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

    // Keep the loaded inputs together so either the demo or a file can use DP.
    private static class ProblemData {
        final String name;
        final String[] itemNames;
        final int[] weights;
        final int[] values;
        final int capacity;

        ProblemData(String name, String[] itemNames, int[] weights, int[] values, int capacity) {
            this.name = name;
            this.itemNames = itemNames;
            this.weights = weights;
            this.values = values;
            this.capacity = capacity;
        }
    }
}

