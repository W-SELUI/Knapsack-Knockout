import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Load benchmark inputs and look up their published answers.
public class BenchmarkReader {
    // Read the provided .kp format: headers, then index/weight/profit item rows.
    // OPTIMAL metadata is ignored: it is not an input to the DP calculation.
    public static ProblemData readProblem(Path file) throws IOException {
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
            } 
            
            else {
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
    public static Integer readKnownOptimum(Path problemFile) throws IOException {
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

}
