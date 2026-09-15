// Keep a problem's input data together for the reader, solver and printer.
public class ProblemData {
    final String name;
    final String[] itemNames;
    final int[] weights;
    final int[] values;
    final int capacity;

    public ProblemData(String name, String[] itemNames, int[] weights, int[] values, int capacity) {
        this.name = name;
        this.itemNames = itemNames;
        this.weights = weights;
        this.values = values;
        this.capacity = capacity;
    }
}
