// Run this file for our camera, speaker and console example.
public class Demo {
    public static void main(String[] args) {
        // Matching positions describe the same item in all three arrays.
        ProblemData problem = new ProblemData("Camera example",
                new String[]{"Camera", "Speaker", "Console"},
                new int[]{2, 3, 4},
                new int[]{3, 4, 5},
                5);

        int[][] dp = DPSolver.buildTable(problem);
        boolean[] selectedItems = DPSolver.findSelectedItems(
                dp, problem.weights, problem.capacity);
        int bestValue = dp[problem.weights.length][problem.capacity];

        TablePrinter.printProblem(problem, null);
        TablePrinter.printTable(dp, problem.itemNames, problem.capacity, true);
        TablePrinter.printResult(problem, bestValue, selectedItems);
    }
}
