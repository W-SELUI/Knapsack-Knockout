import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * A small Swing dashboard for Q4. It keeps the graph itself separate from
 * the controls so the user can choose a benchmark and an algorithm mode
 * without changing any of the Q2 or Q3 measurement code.
 */
public final class GraphDashboard {
    private static final String DEFAULT_DIRECTORY = "benchmarks/benchmarks";
    private static final String[] BENCHMARK_NAMES = {
            "p01.kp", "p02.kp", "p03.kp", "p04.kp",
            "p05.kp", "p06.kp", "p07.kp", "p08.kp"
    };
    private static final long GA_GRAPH_SEED = 20260926L;

    private static final Color NAVY = new Color(24, 34, 56);
    private static final Color BLUE = new Color(47, 107, 255);
    private static final Color RED = new Color(224, 90, 71);
    private static final Color MUTED = new Color(93, 103, 121);
    private static final Color CARD_BORDER = new Color(218, 224, 234);

    private final JFrame frame = new JFrame();
    private final JComboBox<ProblemOption> problemBox = new JComboBox<>();
    private final JComboBox<RunMode> modeBox = new JComboBox<>(RunMode.values());
    private final JSlider speedSlider = new JSlider(5, 120, 35);
    private final JLabel speedLabel = new JLabel("35 ms");
    private final JLabel statusLabel = new JLabel("Ready. Select a problem and press Start.");
    private final JLabel knownLabel = new JLabel("—");
    private final JLabel dpLabel = new JLabel("Waiting");
    private final JLabel gaLabel = new JLabel("Waiting");
    private final JPanel chartHolder = new JPanel(new BorderLayout());
    private final JButton startButton = new JButton("Start comparison");
    private final JButton clearButton = new JButton("Clear graph");

    private LiveGraphPanel chart;
    private boolean running;
    private long runToken;

    private GraphDashboard(String[] args) {
        loadProblemOptions(args);
        buildWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new GraphDashboard(args).show();
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(null, error.getMessage(),
                        "Could not start live graph", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void loadProblemOptions(String[] args) {
        List<ProblemOption> options = new ArrayList<>();
        Path defaultDirectory = Path.of(DEFAULT_DIRECTORY);
        for (String name : BENCHMARK_NAMES) {
            options.add(new ProblemOption(defaultDirectory.resolve(name)));
        }

        int selectedIndex = 0;
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Use: java -cp out GraphRunner [path-to-problem.kp]");
        }
        if (args.length == 1) {
            Path requested = Path.of(args[0]);
            String requestedName = requested.getFileName().toString();
            boolean found = false;
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).path.getFileName().toString()
                        .equalsIgnoreCase(requestedName)) {
                    selectedIndex = i;
                    found = true;
                    break;
                }
            }
            if (!found) {
                options.add(0, new ProblemOption(requested));
                selectedIndex = 0;
            }
        }

        for (ProblemOption option : options) {
            problemBox.addItem(option);
        }
        problemBox.setSelectedIndex(Math.min(selectedIndex, problemBox.getItemCount() - 1));
    }

    private void buildWindow() {
        frame.setTitle("Knapsack Knockout | Live Algorithm Comparison");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1080, 760));
        frame.setSize(1180, 820);
        frame.setLayout(new BorderLayout(0, 0));

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildMainContent(), BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);

        problemBox.addActionListener(this::problemChanged);
        modeBox.addActionListener(event -> updateModeLabels());
        speedSlider.addChangeListener(event -> {
            speedLabel.setText(speedSlider.getValue() + " ms");
        });
        startButton.addActionListener(event -> startRun());
        clearButton.addActionListener(event -> clearGraph());

        updateModeLabels();
        resetCards();
        chart = new LiveGraphPanel("Choose a problem and press Start", 1, true, true);
        chartHolder.add(chart, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 17, 28));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Knapsack Knockout");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        JLabel subtitle = new JLabel(
                "CS214  •  0/1 knapsack  •  live comparison of exact DP and heuristic GA");
        subtitle.setForeground(new Color(205, 215, 233));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        return header;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(new Color(242, 245, 250));
        content.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));
        content.add(buildControls(), BorderLayout.NORTH);

        chartHolder.setOpaque(false);
        chartHolder.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        content.add(chartHolder, BorderLayout.CENTER);
        content.add(buildResultCards(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel buildControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        controls.setBackground(Color.WHITE);
        controls.setBorder(cardBorder());

        controls.add(label("Problem"));
        problemBox.setPreferredSize(new Dimension(178, 30));
        controls.add(problemBox);

        controls.add(label("Run"));
        modeBox.setPreferredSize(new Dimension(205, 30));
        controls.add(modeBox);

        controls.add(label("Animation speed"));
        speedSlider.setPreferredSize(new Dimension(135, 30));
        speedSlider.setToolTipText("Delay between visible progress points; this affects the demo only");
        controls.add(speedSlider);
        speedLabel.setPreferredSize(new Dimension(48, 25));
        speedLabel.setForeground(MUTED);
        controls.add(speedLabel);

        startButton.setBackground(new Color(42, 111, 224));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD));
        startButton.setMargin(new Insets(7, 14, 7, 14));
        controls.add(startButton);

        clearButton.setFocusPainted(false);
        clearButton.setMargin(new Insets(7, 12, 7, 12));
        controls.add(clearButton);
        return controls;
    }

    private JPanel buildResultCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setOpaque(false);
        cards.add(resultCard("Known optimum", knownLabel, new Color(91, 99, 115)));
        cards.add(resultCard("Dynamic Programming", dpLabel, BLUE));
        cards.add(resultCard("Genetic Algorithm", gaLabel, RED));
        return cards;
    }

    private JPanel resultCard(String heading, JLabel value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(8, 3));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                cardBorder(), BorderFactory.createEmptyBorder(9, 13, 9, 13)));
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(5, 42));
        card.add(stripe, BorderLayout.WEST);

        JLabel title = new JLabel(heading);
        title.setForeground(MUTED);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 12f));
        value.setForeground(new Color(33, 40, 56));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 14f));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(3));
        text.add(value);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
        footer.add(statusLabel, BorderLayout.WEST);
        JLabel definitions = new JLabel("NFC = number of core calculations");
        definitions.setForeground(MUTED);
        definitions.setFont(definitions.getFont().deriveFont(Font.PLAIN, 11f));
        footer.add(definitions, BorderLayout.EAST);
        return footer;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }

    private Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    private void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void problemChanged(ActionEvent ignored) {
        if (!running) {
            ProblemOption option = selectedOption();
            knownLabel.setText(option == null || option.knownOptimal == null
                    ? "Unavailable" : format(option.knownOptimal));
            statusLabel.setText("Ready to run " + (option == null ? "the selected problem" : option.fileName()));
        }
    }

    private void startRun() {
        if (running) return;
        ProblemOption option = selectedOption();
        RunMode mode = (RunMode) modeBox.getSelectedItem();
        if (option == null || mode == null) return;

        final ProblemData problem;
        final int knownOptimum;
        try {
            problem = BenchmarkReader.readProblem(option.path);
            Integer known = BenchmarkReader.readKnownOptimum(option.path);
            if (known == null) {
                throw new IllegalArgumentException("No known optimum was found for " + option.path);
            }
            knownOptimum = known;
        } catch (IOException | IllegalArgumentException error) {
            showError(error.getMessage());
            return;
        }

        final boolean runDp = mode.runsDp();
        final boolean runGa = mode.runsGa();
        final int algorithmCount = (runDp ? 1 : 0) + (runGa ? 1 : 0);
        final AtomicInteger remaining = new AtomicInteger(algorithmCount);
        final long thisRun = ++runToken;
        final int displayDelay = speedSlider.getValue();

        chart = new LiveGraphPanel(option.fileName(), knownOptimum, runDp, runGa);
        chartHolder.removeAll();
        chartHolder.add(chart, BorderLayout.CENTER);
        chartHolder.revalidate();
        chartHolder.repaint();
        resetCards();
        knownLabel.setText(format(knownOptimum));
        setRunning(true);
        statusLabel.setText("Running " + mode.displayName + " on " + option.fileName() + "...");

        ProgressListener listener = (algorithm, nfc, profit) -> {
            if (thisRun != runToken) return;
            SwingUtilities.invokeLater(() -> {
                if (thisRun == runToken) chart.addPoint(algorithm, nfc, profit);
            });
            pauseForDisplay(displayDelay);
        };

        if (runDp) {
            Thread dpThread = new Thread(() -> {
                try {
                    long start = System.nanoTime();
                    DPSolver.CompactResult result = DPSolver.solveCompact(problem, listener);
                    double timeMs = elapsedMilliseconds(start);
                    SwingUtilities.invokeLater(() -> {
                        if (thisRun != runToken) return;
                        chart.markFinished("DP");
                        dpLabel.setText(summary(result.getBestValue(), result.getTotalWeight(),
                                result.getNfc(), timeMs));
                        finishAlgorithm(remaining, mode, thisRun);
                    });
                } catch (RuntimeException error) {
                    reportAlgorithmError("DP", error, remaining, mode, thisRun);
                }
            }, "q4-dp");
            dpThread.start();
        }

        if (runGa) {
            Thread gaThread = new Thread(() -> {
                try {
                    long start = System.nanoTime();
                    GeneticAlgorithm algorithm = new GeneticAlgorithm(
                            problem,
                            GeneticAlgorithm.DEFAULT_POPULATION_SIZE,
                            GeneticAlgorithm.DEFAULT_GENERATIONS,
                            GeneticAlgorithm.DEFAULT_MUTATION_RATE,
                            new Random(GA_GRAPH_SEED));
                    GAResult result = algorithm.solve(listener);
                    double timeMs = elapsedMilliseconds(start);
                    SwingUtilities.invokeLater(() -> {
                        if (thisRun != runToken) return;
                        chart.markFinished("GA");
                        gaLabel.setText(summary(result.getFitness(), result.getTotalWeight(),
                                result.getFitnessEvaluations(), timeMs));
                        finishAlgorithm(remaining, mode, thisRun);
                    });
                } catch (RuntimeException error) {
                    reportAlgorithmError("GA", error, remaining, mode, thisRun);
                }
            }, "q4-ga");
            gaThread.start();
        }
    }

    private void reportAlgorithmError(String algorithm, RuntimeException error,
            AtomicInteger remaining, RunMode mode, long thisRun) {
        SwingUtilities.invokeLater(() -> {
            if (thisRun != runToken) return;
            chart.showError(algorithm + " error: " + error.getMessage());
            if (algorithm.equals("DP")) dpLabel.setText("Error");
            if (algorithm.equals("GA")) gaLabel.setText("Error");
            finishAlgorithm(remaining, mode, thisRun);
        });
    }

    private void finishAlgorithm(AtomicInteger remaining, RunMode mode, long thisRun) {
        if (remaining.decrementAndGet() == 0 && thisRun == runToken) {
            running = false;
            setRunning(false);
            statusLabel.setText(mode.displayName + " finished. The graph shows the final best-so-far profit.");
        }
    }

    private void clearGraph() {
        if (running) return;
        runToken++;
        chart = new LiveGraphPanel("Choose a problem and press Start", 1, true, true);
        chartHolder.removeAll();
        chartHolder.add(chart, BorderLayout.CENTER);
        chartHolder.revalidate();
        chartHolder.repaint();
        resetCards();
        problemChanged(null);
    }

    private void setRunning(boolean value) {
        running = value;
        problemBox.setEnabled(!value);
        modeBox.setEnabled(!value);
        speedSlider.setEnabled(!value);
        startButton.setEnabled(!value);
        clearButton.setEnabled(!value);
    }

    private void resetCards() {
        ProblemOption option = selectedOption();
        knownLabel.setText(option == null || option.knownOptimal == null
                ? "Unavailable" : format(option.knownOptimal));
        dpLabel.setText("Waiting");
        gaLabel.setText("Waiting");
    }

    private void updateModeLabels() {
        RunMode mode = (RunMode) modeBox.getSelectedItem();
        if (mode != null && !running) {
            statusLabel.setText("Ready to run " + mode.displayName + ".");
        }
    }

    private ProblemOption selectedOption() {
        return (ProblemOption) problemBox.getSelectedItem();
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        JOptionPane.showMessageDialog(frame, message, "Cannot run graph", JOptionPane.ERROR_MESSAGE);
    }

    private static String summary(int profit, int weight, long nfc, double timeMs) {
        return "Profit " + format(profit) + "  •  Weight " + format(weight)
                + "  •  NFC " + format(nfc) + "  •  " + String.format(Locale.ROOT, "%.2f ms", timeMs);
    }

    private static String format(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(value);
    }

    private static double elapsedMilliseconds(long start) {
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private static void pauseForDisplay(int delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private enum RunMode {
        BOTH("DP + GA comparison", true, true),
        DP_ONLY("Dynamic Programming only", true, false),
        GA_ONLY("Genetic Algorithm only", false, true);

        private final String displayName;
        private final boolean dp;
        private final boolean ga;

        RunMode(String displayName, boolean dp, boolean ga) {
            this.displayName = displayName;
            this.dp = dp;
            this.ga = ga;
        }

        private boolean runsDp() { return dp; }
        private boolean runsGa() { return ga; }

        @Override
        public String toString() { return displayName; }
    }

    private static final class ProblemOption {
        private final Path path;
        private final Integer knownOptimal;

        private ProblemOption(Path path) {
            this.path = path;
            Integer known = null;
            try {
                if (Files.exists(path)) known = BenchmarkReader.readKnownOptimum(path);
            } catch (IOException | RuntimeException ignored) {
                // The full error is shown when the user presses Start.
            }
            this.knownOptimal = known;
        }

        private String fileName() {
            return path.getFileName() == null ? path.toString() : path.getFileName().toString();
        }

        @Override
        public String toString() {
            return fileName().toUpperCase(Locale.ROOT);
        }
    }
}
